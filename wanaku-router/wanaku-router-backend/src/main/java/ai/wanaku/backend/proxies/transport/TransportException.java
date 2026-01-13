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

/**
 * Base exception for transport-related errors.
 * <p>
 * This exception is thrown when a transport operation fails, such as when
 * a connection cannot be established, a request times out, or the remote
 * service returns an error. Specific transport implementations may throw
 * more specific subclasses to provide additional context about the failure.
 * <p>
 * All transport exceptions include the service address to aid in debugging
 * and error reporting.
 */
public class TransportException extends RuntimeException {

    private final String serviceAddress;

    /**
     * Creates a new transport exception with the specified message and service address.
     *
     * @param message the error message
     * @param serviceAddress the address of the service that caused the error
     */
    public TransportException(String message, String serviceAddress) {
        super(message);
        this.serviceAddress = serviceAddress;
    }

    /**
     * Creates a new transport exception with the specified message, service address, and cause.
     *
     * @param message the error message
     * @param serviceAddress the address of the service that caused the error
     * @param cause the underlying cause of the error
     */
    public TransportException(String message, String serviceAddress, Throwable cause) {
        super(message, cause);
        this.serviceAddress = serviceAddress;
    }

    /**
     * Returns the address of the service that caused the error.
     *
     * @return the service address
     */
    public String getServiceAddress() {
        return serviceAddress;
    }

    /**
     * Exception thrown when a transport connection cannot be established.
     * <p>
     * This typically indicates network issues, the service being down,
     * or incorrect service configuration.
     */
    public static class TransportConnectionException extends TransportException {
        /**
         * Creates a new connection exception for the specified service.
         *
         * @param serviceAddress the address of the service
         * @param cause the underlying cause
         */
        public TransportConnectionException(String serviceAddress, Throwable cause) {
            super("Failed to connect to service: " + serviceAddress, serviceAddress, cause);
        }
    }

    /**
     * Exception thrown when a request times out.
     * <p>
     * This indicates that the service did not respond within the expected
     * time frame. This could be due to the service being overloaded, network
     * latency, or the request taking longer than expected to process.
     */
    public static class TransportTimeoutException extends TransportException {
        /**
         * Creates a new timeout exception for the specified service.
         *
         * @param serviceAddress the address of the service
         */
        public TransportTimeoutException(String serviceAddress) {
            super("Request timed out for service: " + serviceAddress, serviceAddress);
        }
    }
}
