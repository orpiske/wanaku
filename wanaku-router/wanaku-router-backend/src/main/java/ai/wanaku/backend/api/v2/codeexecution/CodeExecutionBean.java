/*
 * Copyright 2026 Wanaku AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.wanaku.backend.api.v2.codeexecution;

import ai.wanaku.backend.bridge.CodeExecutorBridge;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.types.execution.CodeExecutionEvent;
import ai.wanaku.capabilities.sdk.api.types.execution.CodeExecutionRequest;
import ai.wanaku.capabilities.sdk.api.types.execution.CodeExecutionResponse;
import ai.wanaku.capabilities.sdk.api.types.execution.CodeExecutionStatus;
import ai.wanaku.capabilities.sdk.api.types.execution.CodeExecutionTask;
import ai.wanaku.core.exchange.CodeExecutionReply;
import ai.wanaku.core.exchange.ExecutionStatus;
import ai.wanaku.core.exchange.OutputType;
import ai.wanaku.core.persistence.infinispan.codeexecution.InfinispanCodeTaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import org.jboss.logging.Logger;

/**
 * Business logic for code execution operations.
 * <p>
 * This bean handles the core business logic for code execution tasks,
 * including task creation, storage, and result streaming via Server-Sent Events.
 * </p>
 * <p>
 * Execution requests are routed to appropriate code execution services discovered
 * via the capabilities repository, with communication handled through the gRPC bridge.
 * </p>
 */
@ApplicationScoped
public class CodeExecutionBean {
    private static final Logger LOG = Logger.getLogger(CodeExecutionBean.class);

    @Inject
    Instance<InfinispanCodeTaskRepository> infinispanCodeTaskRepositoryInstance;

    @Inject
    Instance<CodeExecutorBridge> codeExecutorBridgeInstance;

    private InfinispanCodeTaskRepository taskRepository;

    private CodeExecutorBridge codeExecutionBridge;

    @PostConstruct
    public void init() {
        taskRepository = infinispanCodeTaskRepositoryInstance.get();
        codeExecutionBridge = codeExecutorBridgeInstance.get();
    }

    /**
     * Submits code for execution and creates a new task.
     * <p>
     * This method generates a unique task ID, creates a task object,
     * stores it in memory, and returns a response containing the task ID
     * and SSE stream URL.
     * </p>
     *
     * @param engineType the execution engine type (e.g., "jvm", "interpreted")
     * @param language the programming language (e.g., "java", "python")
     * @param request the code execution request
     * @param baseUrl the base URL for constructing the SSE stream URL
     * @return a response containing task details and SSE URL
     */
    public CodeExecutionResponse submitExecution(
            String engineType, String language, CodeExecutionRequest request, String baseUrl) {

        LOG.infof("Creating code execution task (engine=%s, language=%s)", engineType, language);

        // Create task object
        CodeExecutionTask task = new CodeExecutionTask(null, request, language, engineType);

        // Store task (repository will generate the ID)
        CodeExecutionTask storedTask = taskRepository.store(task);
        String taskId = storedTask.getTaskId();

        // Build SSE stream URL
        String sseUrl =
                String.format("%s/api/v2/code-execution-engine/%s/%s/%s", baseUrl, engineType, language, taskId);

        // Dispatch the task for execution
        codeExecutionBridge.executeCode(engineType, language, request);

        LOG.debugf("Task %s created with SSE URL: %s", taskId, sseUrl);

        // Return response using SDK record factory method
        return CodeExecutionResponse.createPending(taskId, sseUrl);
    }

    /**
     * Streams execution results via Server-Sent Events.
     * <p>
     * This method retrieves the task, executes the code via the gRPC bridge,
     * and streams the execution output through the SSE connection.
     * </p>
     *
     * @param taskId the task UUID
     * @param eventSink the SSE event sink for sending events
     * @param sse the SSE context for creating events
     */
    public void streamExecution(String taskId, SseEventSink eventSink, Sse sse) {
        LOG.infof("Starting SSE stream for task: %s", taskId);

        try {
            // Retrieve task
            Optional<CodeExecutionTask> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                LOG.warnf("Task not found: %s", taskId);
                sendErrorEvent(eventSink, sse, "Task not found: " + taskId);
                return;
            }

            CodeExecutionTask task = taskOpt.get();
            task.markStarted();
            taskRepository.update(task);

            // Send STARTED event
            sendEvent(eventSink, sse, CodeExecutionEvent.started(taskId));

            // Execute code via the bridge
            try {
                Iterator<CodeExecutionReply> replyIterator =
                        codeExecutionBridge.executeCode(task.getEngineType(), task.getLanguage(), task.getRequest());

                int exitCode = 0;
                while (replyIterator.hasNext()) {
                    CodeExecutionReply reply = replyIterator.next();
                    exitCode = processReply(taskId, reply, eventSink, sse);
                }

                // Mark task as completed
                task.markCompleted(exitCode);
                taskRepository.update(task);

                LOG.infof("SSE stream completed for task: %s", taskId);

            } catch (ServiceNotFoundException e) {
                LOG.warnf("No code execution service found for task: %s", taskId);
                task.markCompleted(CodeExecutionStatus.FAILED.ordinal());
                taskRepository.update(task);
                sendErrorEvent(eventSink, sse, e.getMessage());
            }

        } catch (Exception e) {
            LOG.errorf(e, "Error streaming execution for task: %s", taskId);
            sendErrorEvent(eventSink, sse, "Internal error: " + e.getMessage());
        } finally {
            // Always close the event sink
            eventSink.close();
        }
    }

    /**
     * Processes a CodeExecutionReply and sends appropriate SSE events.
     *
     * @param taskId the task ID
     * @param reply the gRPC reply from the code execution service
     * @param eventSink the SSE event sink
     * @param sse the SSE context
     * @return the exit code if this is a completion message, otherwise 0
     */
    private int processReply(String taskId, CodeExecutionReply reply, SseEventSink eventSink, Sse sse) {
        OutputType outputType = reply.getOutputType();
        ExecutionStatus status = reply.getStatus();

        if (reply.getIsError()) {
            String errorContent = reply.getContentCount() > 0 ? reply.getContent(0) : "Unknown error";
            sendEvent(eventSink, sse, CodeExecutionEvent.output(taskId, "ERROR: " + errorContent + "\n"));
            return reply.getExitCode();
        }

        // Send output content
        for (String content : reply.getContentList()) {
            if (outputType == OutputType.STDERR) {
                sendEvent(eventSink, sse, CodeExecutionEvent.output(taskId, "STDERR: " + content));
            } else {
                sendEvent(eventSink, sse, CodeExecutionEvent.output(taskId, content));
            }
        }

        // Check for completion
        if (outputType == OutputType.COMPLETION || status == ExecutionStatus.COMPLETED) {
            sendEvent(eventSink, sse, CodeExecutionEvent.completed(taskId, reply.getExitCode()));
            return reply.getExitCode();
        }

        if (status == ExecutionStatus.FAILED) {
            String errorMsg = reply.getContentCount() > 0 ? reply.getContent(0) : "Execution failed";
            sendEvent(eventSink, sse, CodeExecutionEvent.failed(taskId, reply.getExitCode(), errorMsg));
            return reply.getExitCode();
        }

        return 0;
    }

    /**
     * Sends a CodeExecutionEvent through the SSE connection.
     *
     * @param sink the SSE event sink
     * @param sse the SSE context
     * @param event the CodeExecutionEvent to send
     */
    private void sendEvent(SseEventSink sink, Sse sse, CodeExecutionEvent event) {
        String eventName = event.getEventType().name().toLowerCase();

        OutboundSseEvent sseEvent =
                sse.newEventBuilder().name(eventName).data(event).build();

        sink.send(sseEvent);
        LOG.debugf("Sent %s event for task: %s", eventName, event.getTaskId());
    }

    /**
     * Sends an error event through the SSE connection.
     *
     * @param sink the SSE event sink
     * @param sse the SSE context
     * @param errorMessage the error message
     */
    private void sendErrorEvent(SseEventSink sink, Sse sse, String errorMessage) {
        CodeExecutionEvent event = CodeExecutionEvent.failed(UUID.randomUUID().toString(), -1, errorMessage);

        OutboundSseEvent sseEvent =
                sse.newEventBuilder().name("error").data(event).build();

        sink.send(sseEvent);
        LOG.debugf("Sent error event: %s", errorMessage);
    }
}
