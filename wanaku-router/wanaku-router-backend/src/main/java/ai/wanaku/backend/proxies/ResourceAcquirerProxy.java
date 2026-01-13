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
import io.grpc.ManagedChannelBuilder;
import io.quarkiverse.mcp.server.ResourceContents;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.TextResourceContents;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;

/**
 * A proxy class for acquiring resources
 */
public class ResourceAcquirerProxy extends AbstractGrpcProxy implements ResourceProxy {
    private static final Logger LOG = Logger.getLogger(ResourceAcquirerProxy.class);
    private static final String EMPTY_ARGUMENT = "";

    public ResourceAcquirerProxy(ServiceResolver serviceResolver) {
        super(serviceResolver);
    }

    @Override
    public List<ResourceContents> eval(ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {
        LOG.infof(
                "Requesting resource on behalf of connection %s",
                arguments.connection().id());

        ServiceTarget service = serviceResolver.resolve(mcpResource.getType(), ServiceType.RESOURCE_PROVIDER);
        if (service == null) {
            String message = String.format("There is no service registered for service %s", mcpResource.getType());
            LOG.error(message);

            TextResourceContents textResourceContents =
                    new TextResourceContents(arguments.requestUri().value(), message, "text/plain");
            return List.of(textResourceContents);
        }

        LOG.infof("Requesting %s from %s", mcpResource.getName(), service.toAddress());
        final ResourceReply reply = acquireRemotely(mcpResource, arguments, service);
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
    public String name() {
        return "resource-acquirer";
    }

    private ResourceReply acquireRemotely(
            ResourceReference mcpResource, ResourceManager.ResourceArguments arguments, ServiceTarget service) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(service.toAddress())
                .usePlaintext()
                .build();

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

    @Override
    public ProvisioningReference provision(ResourcePayload payload) {
        ResourceReference resourceReference = payload.getPayload();

        return performProvisioning(
                resourceReference.getName(),
                resourceReference.getType(),
                payload.getConfigurationData(),
                payload.getSecretsData(),
                ServiceType.RESOURCE_PROVIDER);
    }
}
