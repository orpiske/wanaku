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

package ai.wanaku.backend.proxies.transport.grpc;

import ai.wanaku.backend.proxies.transport.TransportException;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.core.exchange.ToolInvokeReply;
import ai.wanaku.core.exchange.ToolInvokeRequest;
import ai.wanaku.core.exchange.ToolInvokerGrpc;
import io.grpc.ManagedChannel;
import org.jboss.logging.Logger;

/**
 * gRPC transport implementation for tool invocation operations.
 * <p>
 * This transport handles communication with remote tool invoker services
 * using the gRPC ToolInvoker service interface. It creates channels, sends
 * tool invocation requests, and receives responses.
 * <p>
 * Thread Safety: This class is thread-safe through synchronization inherited
 * from the base GrpcTransport class.
 */
public class GrpcToolInvokerTransport extends GrpcTransport<ToolInvokeRequest, ToolInvokeReply> {
    private static final Logger LOG = Logger.getLogger(GrpcToolInvokerTransport.class);

    /**
     * Sends a tool invocation request to the remote service.
     * <p>
     * This method creates a gRPC channel to the service, creates a blocking stub,
     * sends the request, and returns the reply. The channel is reused if multiple
     * requests are sent to the same service.
     *
     * @param request the tool invocation request
     * @param service the target service
     * @return the tool invocation reply
     * @throws TransportException if the request fails
     */
    @Override
    public ToolInvokeReply send(ToolInvokeRequest request, ServiceTarget service) throws TransportException {
        LOG.debugf("Sending tool invocation request to service: %s", service.toAddress());

        ManagedChannel channel = getOrCreateChannel(service);

        try {
            ToolInvokerGrpc.ToolInvokerBlockingStub stub = ToolInvokerGrpc.newBlockingStub(channel);
            ToolInvokeReply reply = stub.invokeTool(request);

            LOG.debugf(
                    "Received tool invocation reply from service: %s (isError: %s)",
                    service.toAddress(), reply.getIsError());

            return reply;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to invoke tool on service: %s", service.toAddress());
            throw new TransportException("Tool invocation failed", service.toAddress(), e);
        }
    }
}
