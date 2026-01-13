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

import ai.wanaku.backend.proxies.transport.ProxyTransport;
import ai.wanaku.backend.proxies.transport.TransportException;
import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceUnavailableException;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.PayloadType;
import ai.wanaku.core.exchange.Secret;
import java.util.Objects;
import org.jboss.logging.Logger;

/**
 * Abstract base class for transport-agnostic proxy implementations.
 * <p>
 * This class decouples proxy business logic from transport mechanisms by
 * accepting a {@link ProxyTransport} implementation via dependency injection.
 * This allows proxies to work with any transport (gRPC, HTTP, STDIO, etc.)
 * without modification to the proxy logic.
 * <p>
 * This class follows the Template Method pattern, providing concrete
 * implementations of common operations while allowing subclasses to
 * implement service-specific behavior.
 * <p>
 * The class manages two key responsibilities:
 * <ul>
 *   <li>Service resolution via {@link ServiceResolver}</li>
 *   <li>Transport operations via {@link ProxyTransport}</li>
 * </ul>
 * <p>
 * Subclasses should focus on implementing their specific proxy interface
 * methods and delegate common operations to the protected methods provided
 * by this base class.
 *
 * @param <REQ> the request type for the transport
 * @param <RESP> the response type for the transport
 * @see InvokerProxy
 * @see ResourceAcquirerProxy
 */
public abstract class AbstractProxy<REQ, RESP> implements Proxy {
    private static final Logger LOG = Logger.getLogger(AbstractProxy.class);

    protected final ServiceResolver serviceResolver;
    protected final ProxyTransport<REQ, RESP> transport;

    /**
     * Creates a new AbstractProxy with the specified service resolver and transport.
     * <p>
     * This constructor enables dependency injection of both service resolution
     * and transport mechanisms, making the proxy fully decoupled from specific
     * implementations.
     *
     * @param serviceResolver the resolver for locating services
     * @param transport the transport for communicating with services
     */
    protected AbstractProxy(ServiceResolver serviceResolver, ProxyTransport<REQ, RESP> transport) {
        this.serviceResolver = serviceResolver;
        this.transport = transport;
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
     * Sends a request to the target service using the configured transport.
     * <p>
     * This method delegates to the transport and converts any transport
     * exceptions to service exceptions for consistent error handling.
     *
     * @param request the request to send
     * @param service the target service
     * @return the response from the service
     * @throws ServiceUnavailableException if the service cannot be reached
     */
    protected RESP sendRequest(REQ request, ServiceTarget service) {
        try {
            return transport.send(request, service);
        } catch (TransportException e) {
            LOG.errorf(e, "Transport failed for service: %s", service.toAddress());
            throw ServiceUnavailableException.forAddress(service.toAddress());
        }
    }

    /**
     * Provisions configuration and secrets to a remote service.
     * <p>
     * This method builds configuration and secret objects and delegates
     * to the transport for provisioning. It returns a reference containing
     * URIs for accessing the provisioned resources.
     *
     * @param name the name of the configuration/secret
     * @param configData the configuration data (may be null)
     * @param secretsData the secrets data (may be null)
     * @param service the target service
     * @return a provisioning reference with URIs and properties
     * @throws ServiceUnavailableException if provisioning fails
     */
    protected ProvisioningReference provisionResource(
            String name, String configData, String secretsData, ServiceTarget service) {

        LOG.debugf("Provisioning '%s' to service: %s", name, service.toAddress());

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

        try {
            return transport.provision(cfg, secret, service);
        } catch (TransportException e) {
            LOG.errorf(e, "Provisioning failed for service: %s", service.toAddress());
            throw ServiceUnavailableException.forAddress(service.toAddress());
        }
    }
}
