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

import ai.wanaku.backend.proxies.transport.ProxyTransport;
import ai.wanaku.backend.proxies.transport.TransportException;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.ProvisionReply;
import ai.wanaku.core.exchange.ProvisionRequest;
import ai.wanaku.core.exchange.ProvisionerGrpc;
import ai.wanaku.core.exchange.Secret;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.jboss.logging.Logger;

/**
 * Base implementation of gRPC transport for proxy communication.
 * <p>
 * This class provides common gRPC infrastructure for all gRPC-based transports,
 * including channel management, provisioning, and connection lifecycle. It
 * consolidates functionality previously spread across GrpcChannelManager and
 * ProvisioningService.
 * <p>
 * Subclasses should implement the {@link #send(Object, ServiceTarget)} method
 * to provide protocol-specific request handling using the appropriate gRPC stub.
 * <p>
 * Thread Safety: This class is thread-safe. Channel creation is synchronized
 * to prevent race conditions when multiple threads attempt to send requests
 * simultaneously.
 *
 * @param <REQ> the gRPC request type (e.g., ToolInvokeRequest, ResourceRequest)
 * @param <RESP> the gRPC response type (e.g., ToolInvokeReply, ResourceReply)
 */
public abstract class GrpcTransport<REQ, RESP> implements ProxyTransport<REQ, RESP> {
    private static final Logger LOG = Logger.getLogger(GrpcTransport.class);
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private ManagedChannel currentChannel;
    private ServiceTarget currentService;

    /**
     * Creates a new gRPC channel for the specified service target.
     * <p>
     * The channel is configured with plaintext communication. This method
     * is synchronized to ensure thread safety when creating channels.
     *
     * @param service the service target containing the address to connect to
     * @return a new ManagedChannel configured for the service
     */
    protected synchronized ManagedChannel createChannel(ServiceTarget service) {
        LOG.debugf("Creating gRPC channel for service: %s", service.toAddress());
        return ManagedChannelBuilder.forTarget(service.toAddress())
                .usePlaintext()
                .build();
    }

    /**
     * Closes a gRPC channel gracefully.
     * <p>
     * This method attempts to shutdown the channel with a timeout. If the
     * channel does not shut down within the timeout, it is forcibly terminated.
     *
     * @param channel the channel to close, may be null
     */
    protected void closeChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            try {
                LOG.debugf("Closing gRPC channel");
                channel.shutdown();
                if (!channel.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    LOG.warnf(
                            "Channel did not terminate within %d seconds, forcing shutdown", SHUTDOWN_TIMEOUT_SECONDS);
                    channel.shutdownNow();
                }
            } catch (Exception e) {
                LOG.warnf(e, "Error closing gRPC channel");
            }
        }
    }

    /**
     * Provisions configuration and secrets to a remote service via gRPC.
     * <p>
     * This method creates a provisioning request, sends it to the service,
     * and returns a reference containing URIs for accessing the provisioned
     * resources.
     *
     * @param configuration the configuration to provision
     * @param secret the secrets to provision
     * @param service the target service
     * @return a provisioning reference with URIs and properties
     * @throws TransportException if provisioning fails
     */
    @Override
    public ProvisioningReference provision(Configuration configuration, Secret secret, ServiceTarget service)
            throws TransportException {
        LOG.debugf("Provisioning configuration '%s' to service: %s", configuration.getName(), service.toAddress());

        ManagedChannel channel = createChannel(service);

        try {
            ProvisionRequest request = ProvisionRequest.newBuilder()
                    .setConfiguration(configuration)
                    .setSecret(secret)
                    .build();

            ProvisionerGrpc.ProvisionerBlockingStub stub = ProvisionerGrpc.newBlockingStub(channel);
            ProvisionReply reply = stub.provision(request);

            LOG.debugf(
                    "Successfully provisioned configuration '%s' (config URI: %s, secret URI: %s)",
                    configuration.getName(), reply.getConfigurationUri(), reply.getSecretUri());

            return new ProvisioningReference(
                    URI.create(reply.getConfigurationUri()),
                    URI.create(reply.getSecretUri()),
                    reply.getPropertiesMap());
        } catch (Exception e) {
            LOG.errorf(
                    e,
                    "Failed to provision configuration '%s' to service: %s",
                    configuration.getName(),
                    service.toAddress());
            throw new TransportException("Provisioning failed", service.toAddress(), e);
        } finally {
            closeChannel(channel);
        }
    }

    /**
     * Checks if the transport is healthy and ready to send requests.
     * <p>
     * A gRPC transport is considered healthy if it can create channels.
     * This is a simple health check that does not verify connectivity.
     *
     * @return true if healthy, false otherwise
     */
    @Override
    public boolean isHealthy() {
        // Simple health check - can be enhanced to verify actual connectivity
        return true;
    }

    /**
     * Closes the transport and releases any resources.
     * <p>
     * This method closes the current channel if one is open. It is safe
     * to call this method multiple times.
     */
    @Override
    public void close() {
        closeChannel(currentChannel);
        currentChannel = null;
        currentService = null;
    }

    /**
     * Gets or creates a channel for the specified service.
     * <p>
     * This method reuses the current channel if it's for the same service,
     * otherwise it creates a new channel.
     *
     * @param service the target service
     * @return a managed channel for the service
     */
    protected synchronized ManagedChannel getOrCreateChannel(ServiceTarget service) {
        if (currentChannel == null
                || currentService == null
                || !currentService.equals(service)
                || currentChannel.isShutdown()) {
            if (currentChannel != null) {
                closeChannel(currentChannel);
            }
            currentChannel = createChannel(service);
            currentService = service;
        }
        return currentChannel;
    }
}
