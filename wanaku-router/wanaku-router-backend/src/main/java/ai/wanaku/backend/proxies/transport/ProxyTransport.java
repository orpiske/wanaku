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

package ai.wanaku.backend.proxies.transport;

import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.Secret;

/**
 * Transport abstraction for proxy communication with backend services.
 * <p>
 * This interface decouples the transport mechanism from business logic,
 * allowing different transport implementations (gRPC, HTTP, STDIO) to be
 * used interchangeably. It follows the Strategy pattern, enabling runtime
 * selection of transport protocols based on service requirements.
 * <p>
 * Implementations are responsible for:
 * <ul>
 *   <li>Establishing and managing connections to backend services</li>
 *   <li>Sending requests and receiving responses using the appropriate protocol</li>
 *   <li>Provisioning configuration and secrets to remote services</li>
 *   <li>Handling protocol-specific error conditions</li>
 *   <li>Resource cleanup via the {@link AutoCloseable} interface</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * ProxyTransport<ToolInvokeRequest, ToolInvokeReply> transport = new GrpcToolInvokerTransport();
 * try {
 *     ToolInvokeReply response = transport.send(request, serviceTarget);
 *     // Process response...
 * } finally {
 *     transport.close();
 * }
 * }</pre>
 *
 * @param <REQ> the request type for this transport
 * @param <RESP> the response type for this transport
 */
public interface ProxyTransport<REQ, RESP> extends AutoCloseable {

    /**
     * Sends a request to the target service and returns a response.
     * <p>
     * This method handles the complete request-response cycle, including
     * connection establishment, request serialization, response deserialization,
     * and error handling. The implementation should be thread-safe if the
     * transport supports concurrent requests.
     *
     * @param request the request to send
     * @param service the target service
     * @return the response from the service
     * @throws TransportException if communication fails
     */
    RESP send(REQ request, ServiceTarget service) throws TransportException;

    /**
     * Provisions configuration and secrets to a remote service.
     * <p>
     * This method establishes a connection to the target service and sends
     * the configuration and secrets for provisioning. The service will store
     * these values and return URIs that can be used to reference them in
     * subsequent requests.
     *
     * @param configuration the configuration to provision
     * @param secret the secrets to provision
     * @param service the target service
     * @return a provisioning reference with URIs and properties
     * @throws TransportException if provisioning fails
     */
    ProvisioningReference provision(Configuration configuration, Secret secret, ServiceTarget service)
            throws TransportException;

    /**
     * Checks if the transport is healthy and ready to send requests.
     * <p>
     * This method can be used for health checks and monitoring. A healthy
     * transport should be able to establish connections and send requests.
     * This method should not throw exceptions; instead, it should return
     * false if the transport is unhealthy.
     *
     * @return true if healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Returns the name of this transport implementation.
     * <p>
     * This method is useful for logging and debugging purposes. The default
     * implementation returns the simple class name.
     *
     * @return the transport name
     */
    default String name() {
        return this.getClass().getSimpleName();
    }

    /**
     * Closes the transport and releases any resources.
     * <p>
     * This method should be called when the transport is no longer needed.
     * It should close all connections, release threads, and perform any
     * other cleanup necessary. After calling this method, the transport
     * should not be used again.
     * <p>
     * Implementations should ensure this method is idempotent and does not
     * throw exceptions.
     */
    @Override
    void close();
}
