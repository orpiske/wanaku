/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.wanaku.core.mcp.common;

import ai.wanaku.capabilities.sdk.api.types.CallableReference;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkiverse.mcp.server.ToolResponse;
import java.util.Objects;

/**
 * Adapter that bridges ToolExecutor to the Tool interface.
 * <p>
 * This adapter allows ToolExecutor implementations to be used wherever
 * the Tool interface is expected, maintaining backward compatibility
 * while enabling separation of concerns through the composition pattern.
 * <p>
 * The adapter delegates all tool invocation calls to the underlying
 * ToolExecutor, effectively translating between the two interfaces.
 *
 * @see Tool
 * @see ToolExecutor
 */
public class ToolAdapter implements Tool {
    private final ToolExecutor executor;

    /**
     * Creates a new ToolAdapter wrapping the specified executor.
     *
     * @param executor the tool executor to delegate calls to
     * @throws NullPointerException if executor is null
     */
    public ToolAdapter(ToolExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "ToolExecutor cannot be null");
    }

    /**
     * Invokes the tool by delegating to the underlying executor.
     *
     * @param toolArguments the arguments to pass to the tool
     * @param toolReference the reference to the tool being called
     * @return a tool response containing the execution results
     */
    @Override
    public ToolResponse call(ToolManager.ToolArguments toolArguments, CallableReference toolReference) {
        return executor.execute(toolArguments, toolReference);
    }
}
