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

package ai.wanaku.backend.proxies;

import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jboss.logging.Logger;

/**
 * Manages the lifecycle of gRPC channels for proxy implementations.
 * <p>
 * This class is responsible for creating and managing gRPC channels with
 * consistent configuration across all proxy implementations. It provides
 * a centralized point for channel management, making it easier to add
 * features like connection pooling, interceptors, and custom configurations
 * in the future.
 * <p>
 * Current implementation creates channels with plaintext communication.
 * Future enhancements may include:
 * <ul>
 *   <li>Connection pooling for reusing channels</li>
 *   <li>Interceptors for logging and metrics</li>
 *   <li>SSL/TLS configuration</li>
 *   <li>Retry policies and circuit breakers</li>
 * </ul>
 */
public class GrpcChannelManager {
    private static final Logger LOG = Logger.getLogger(GrpcChannelManager.class);

    /**
     * Creates a new gRPC channel for the specified service target.
     * <p>
     * The channel is configured with plaintext communication. The caller
     * is responsible for managing the channel lifecycle, including shutdown.
     *
     * @param service the service target containing the address to connect to
     * @return a new ManagedChannel configured for the service
     */
    public ManagedChannel createChannel(ServiceTarget service) {
        LOG.debugf("Creating gRPC channel for service: %s", service.toAddress());
        return ManagedChannelBuilder.forTarget(service.toAddress())
                .usePlaintext()
                .build();
    }

    /**
     * Closes a gRPC channel gracefully.
     * <p>
     * This method attempts to shutdown the channel. If an error occurs during
     * shutdown, it is logged but not propagated to avoid disrupting the caller.
     *
     * @param channel the channel to close, may be null
     */
    public void closeChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            try {
                LOG.debugf("Closing gRPC channel");
                channel.shutdown();
            } catch (Exception e) {
                LOG.warnf(e, "Error closing gRPC channel");
            }
        }
    }
}
