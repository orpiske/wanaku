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

import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.PayloadType;
import ai.wanaku.core.exchange.Secret;
import io.grpc.ManagedChannel;
import java.util.Objects;
import org.jboss.logging.Logger;

/**
 * Abstract base class for gRPC-based proxy implementations.
 * <p>
 * This class encapsulates common functionality required by all gRPC proxies,
 * including service resolution, channel management, and provisioning operations.
 * It follows the Template Method pattern, providing concrete implementations
 * of common operations while allowing subclasses to implement service-specific
 * behavior.
 * <p>
 * The class manages three key responsibilities:
 * <ul>
 *   <li>Service resolution via {@link ServiceResolver}</li>
 *   <li>gRPC channel lifecycle via {@link GrpcChannelManager}</li>
 *   <li>Configuration provisioning via {@link ProvisioningService}</li>
 * </ul>
 * <p>
 * Subclasses should focus on implementing their specific proxy interface
 * methods and delegate common operations to the protected methods provided
 * by this base class.
 *
 * @see InvokerProxy
 * @see ResourceAcquirerProxy
 */
public abstract class GrpcProxy implements Proxy {
    private static final Logger LOG = Logger.getLogger(GrpcProxy.class);

    protected final ServiceResolver serviceResolver;
    protected final GrpcChannelManager channelManager;
    protected final ProvisioningService provisioningService;

    /**
     * Creates a new GrpcProxy with the specified service resolver.
     * <p>
     * This constructor initializes the channel manager and provisioning
     * service that will be used by all proxy operations.
     *
     * @param serviceResolver the resolver for locating services
     */
    protected GrpcProxy(ServiceResolver serviceResolver) {
        this.serviceResolver = serviceResolver;
        this.channelManager = new GrpcChannelManager();
        this.provisioningService = new ProvisioningService();
    }

    /**
     * Resolves a service target for the specified type and service type.
     * <p>
     * This method uses the service resolver to locate the appropriate service
     * and throws an exception if no service is found.
     *
     * @param type the service type identifier
     * @param serviceType the category of service (e.g., TOOL_INVOKER, RESOURCE_PROVIDER)
     * @return the resolved service target
     * @throws ServiceNotFoundException if no service is registered for the given type
     */
    protected ServiceTarget resolveService(String type, ServiceType serviceType) {
        LOG.debugf("Resolving service for type '%s' and service type '%s'", type, serviceType);
        ServiceTarget service = serviceResolver.resolve(type, serviceType);
        if (service == null) {
            throw new ServiceNotFoundException("There is no host registered for service " + type);
        }
        LOG.debugf("Resolved service: %s", service.toAddress());
        return service;
    }

    /**
     * Creates a new gRPC channel for the specified service.
     * <p>
     * This method delegates to the channel manager to create a channel
     * with consistent configuration.
     *
     * @param service the service target to connect to
     * @return a new managed channel
     */
    protected ManagedChannel createChannel(ServiceTarget service) {
        return channelManager.createChannel(service);
    }

    /**
     * Provisions configuration and secrets to a remote service.
     * <p>
     * This method handles the complete provisioning workflow:
     * <ol>
     *   <li>Creates a gRPC channel to the service</li>
     *   <li>Builds configuration and secret objects</li>
     *   <li>Sends the provisioning request</li>
     *   <li>Returns a reference to the provisioned resources</li>
     * </ol>
     *
     * @param name the name of the configuration/secret
     * @param configData the configuration data (may be null)
     * @param secretsData the secrets data (may be null)
     * @param service the target service
     * @return a provisioning reference with URIs and properties
     */
    protected ProvisioningReference provision(
            String name, String configData, String secretsData, ServiceTarget service) {

        LOG.debugf("Provisioning '%s' to service: %s", name, service.toAddress());

        ManagedChannel channel = createChannel(service);

        Configuration cfg = Configuration.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(name)
                .setPayload(Objects.requireNonNullElse(configData, ""))
                .build();

        Secret secret = Secret.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(name)
                .setPayload(Objects.requireNonNullElse(secretsData, ""))
                .build();

        return provisioningService.provision(cfg, secret, channel, service);
    }
}
