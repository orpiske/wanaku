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
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceUnavailableException;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;
import ai.wanaku.capabilities.sdk.api.types.io.ResourcePayload;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.ResourceAcquirerGrpc;
import ai.wanaku.core.exchange.ResourceReply;
import ai.wanaku.core.exchange.ResourceRequest;
import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.quarkiverse.mcp.server.ResourceContents;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.TextResourceContents;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;

/**
 * A proxy class for acquiring resources via gRPC.
 * <p>
 * This proxy is responsible for provisioning resource configurations and
 * evaluating resource requests by delegating to remote resource providers.
 * <p>
 * This class extends {@link GrpcProxy} to inherit common gRPC functionality
 * such as service resolution, channel management, and provisioning operations.
 * It focuses solely on resource-specific concerns while delegating infrastructure
 * concerns to the base class.
 */
public class ResourceAcquirerProxy extends GrpcProxy implements ResourceProxy {
    private static final Logger LOG = Logger.getLogger(ResourceAcquirerProxy.class);
    private static final String EMPTY_ARGUMENT = "";

    /**
     * Creates a new ResourceAcquirerProxy with the specified service resolver.
     *
     * @param serviceResolver the resolver for locating resource services
     */
    public ResourceAcquirerProxy(ServiceResolver serviceResolver) {
        super(serviceResolver);
    }

    @Override
    public List<ResourceContents> eval(ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {
        LOG.infof(
                "Requesting resource on behalf of connection %s",
                arguments.connection().id());

        ServiceTarget service = resolveService(mcpResource.getType(), ServiceType.RESOURCE_PROVIDER);

        LOG.infof("Requesting %s from %s", mcpResource.getName(), service.toAddress());

        ResourceReply reply = acquireRemotely(mcpResource, arguments, service);

        return processReply(reply, arguments, mcpResource);
    }

    /**
     * Acquires a resource from a remote service via gRPC.
     *
     * @param mcpResource the resource reference
     * @param arguments the resource request arguments
     * @param service the target service
     * @return the resource reply from the remote service
     * @throws ServiceUnavailableException if the service cannot be reached
     */
    private ResourceReply acquireRemotely(
            ResourceReference mcpResource, ResourceManager.ResourceArguments arguments, ServiceTarget service) {

        ManagedChannel channel = createChannel(service);

        ResourceRequest request = ResourceRequest.newBuilder()
                .setLocation(mcpResource.getLocation())
                .setType(mcpResource.getType())
                .setName(mcpResource.getName())
                .setConfigurationURI(Objects.requireNonNullElse(mcpResource.getConfigurationURI(), EMPTY_ARGUMENT))
                .setSecretsURI(Objects.requireNonNullElse(mcpResource.getSecretsURI(), EMPTY_ARGUMENT))
                .build();

        try {
            ResourceAcquirerGrpc.ResourceAcquirerBlockingStub blockingStub =
                    ResourceAcquirerGrpc.newBlockingStub(channel);
            return blockingStub.resourceAcquire(request);
        } catch (Exception e) {
            throw ServiceUnavailableException.forAddress(service.toAddress());
        }
    }

    /**
     * Processes the resource reply and converts it to resource contents.
     *
     * @param reply the reply from the remote service
     * @param arguments the original request arguments
     * @param mcpResource the resource reference
     * @return a list of resource contents
     */
    private List<ResourceContents> processReply(
            ResourceReply reply, ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {

        if (reply.getIsError()) {
            LOG.errorf(
                    "Unable to acquire resource for connection: %s",
                    arguments.connection().id());

            TextResourceContents textResourceContents = new TextResourceContents(
                    arguments.requestUri().value(), reply.getContentList().get(0), "text/plain");
            return List.of(textResourceContents);
        } else {
            ProtocolStringList contentList = reply.getContentList();
            List<ResourceContents> textResourceContentsList = new ArrayList<>();

            for (String content : contentList) {
                TextResourceContents textResourceContents =
                        new TextResourceContents(arguments.requestUri().value(), content, mcpResource.getMimeType());

                textResourceContentsList.add(textResourceContents);
            }

            return textResourceContentsList;
        }
    }

    @Override
    public ProvisioningReference provision(ResourcePayload payload) {
        ResourceReference resourceReference = payload.getPayload();

        LOG.debugf("Provisioning resource: %s (type: %s)", resourceReference.getName(), resourceReference.getType());

        ServiceTarget service = resolveService(resourceReference.getType(), ServiceType.RESOURCE_PROVIDER);

        return provision(
                resourceReference.getName(), payload.getConfigurationData(), payload.getSecretsData(), service);
    }
}
