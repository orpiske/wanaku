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
import ai.wanaku.core.exchange.ResourceAcquirerGrpc;
import ai.wanaku.core.exchange.ResourceReply;
import ai.wanaku.core.exchange.ResourceRequest;
import io.grpc.ManagedChannel;
import org.jboss.logging.Logger;

/**
 * gRPC transport implementation for resource acquisition operations.
 * <p>
 * This transport handles communication with remote resource provider services
 * using the gRPC ResourceAcquirer service interface. It creates channels, sends
 * resource acquisition requests, and receives responses.
 * <p>
 * Thread Safety: This class is thread-safe through synchronization inherited
 * from the base GrpcTransport class.
 */
public class GrpcResourceAcquirerTransport extends GrpcTransport<ResourceRequest, ResourceReply> {
    private static final Logger LOG = Logger.getLogger(GrpcResourceAcquirerTransport.class);

    /**
     * Sends a resource acquisition request to the remote service.
     * <p>
     * This method creates a gRPC channel to the service, creates a blocking stub,
     * sends the request, and returns the reply. The channel is reused if multiple
     * requests are sent to the same service.
     *
     * @param request the resource acquisition request
     * @param service the target service
     * @return the resource acquisition reply
     * @throws TransportException if the request fails
     */
    @Override
    public ResourceReply send(ResourceRequest request, ServiceTarget service) throws TransportException {
        LOG.debugf("Sending resource acquisition request to service: %s", service.toAddress());

        ManagedChannel channel = getOrCreateChannel(service);

        try {
            ResourceAcquirerGrpc.ResourceAcquirerBlockingStub stub = ResourceAcquirerGrpc.newBlockingStub(channel);
            ResourceReply reply = stub.resourceAcquire(request);

            LOG.debugf(
                    "Received resource acquisition reply from service: %s (isError: %s)",
                    service.toAddress(), reply.getIsError());

            return reply;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to acquire resource from service: %s", service.toAddress());
            throw new TransportException("Resource acquisition failed", service.toAddress(), e);
        }
    }
}
