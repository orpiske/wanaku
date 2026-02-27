package ai.wanaku.backend.bridge;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jboss.logging.Logger;
import io.quarkiverse.mcp.server.ResourceContents;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.smallrye.mutiny.Uni;
import ai.wanaku.backend.bridge.transports.grpc.GrpcTransport;
import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;
import ai.wanaku.capabilities.sdk.api.types.io.ResourcePayload;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.v1.ResourceReply;
import ai.wanaku.core.exchange.v1.ResourceRequest;
import com.google.protobuf.ProtocolStringList;

/**
 * A proxy class for acquiring resources via gRPC.
 * <p>
 * This proxy is responsible for provisioning resource configurations and
 * evaluating resource requests by delegating to remote resource providers.
 * <p>
 * This class uses composition to delegate gRPC transport operations to
 * {@link GrpcTransport}, separating business logic from transport concerns.
 * It focuses solely on resource-specific concerns while delegating infrastructure
 * concerns to the transport layer.
 */
public class ResourceAcquirerBridge implements ResourceBridge {
    private static final Logger LOG = Logger.getLogger(ResourceAcquirerBridge.class);
    private static final String EMPTY_ARGUMENT = "";
    private static final String SERVICE_TYPE_RESOURCE_PROVIDER = ServiceType.RESOURCE_PROVIDER.asValue();

    @Inject
    ServiceResolver serviceResolver;

    @Inject
    WanakuBridgeTransport transport;

    // TODO: make configurable
    ExecutorService executor = Executors.newFixedThreadPool(15);

    static class AsyncTransformer {
        CountDownLatch latch = new CountDownLatch(1);
        List<ResourceContents> resourceContentsList;
        SyncTransformer syncTransformer = new SyncTransformer();

        /**
         * Processes the resource reply and converts it to resource contents.
         *
         * @param reply the reply from the remote service
         * @param arguments the original request arguments
         * @param mcpResource the resource reference
         * @return a list of resource contents
         */
        private void processReplyAsync(
                ResourceReply reply, ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {

            try {
                resourceContentsList = syncTransformer.processReply(reply, arguments, mcpResource);
            } finally {
                latch.countDown();
            }
        }

        public List<ResourceContents> getReply() {
            try {
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    LOG.warn("Timeout waiting for reply");
                }
                return resourceContentsList;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return null;
        }
    }

    static class SyncTransformer {

        /**
         * Processes the resource reply and converts it to resource contents.
         *
         * @param reply the reply from the remote service
         * @param arguments the original request arguments
         * @param mcpResource the resource reference
         * @return a list of resource contents
         */
        public List<ResourceContents> processReply(
                ResourceReply reply, ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {
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

    private SyncTransformer responseTransformer = new SyncTransformer();

    /**
     * Creates a new ResourceAcquirerBridge with the specified service resolver and transport.
     * <p>
     * This constructor is primarily intended for testing purposes, allowing
     * injection of a mock or custom transport implementation.
     *
     * @param serviceResolver the resolver for locating resource services
     * @param transport the gRPC transport for communication
     */
    public ResourceAcquirerBridge(ServiceResolver serviceResolver, WanakuBridgeTransport transport) {
        this.serviceResolver = serviceResolver;
        this.transport = transport;
    }

    @Override
    public List<ResourceContents> read(ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {
        LOG.infof(
                "Requesting resource on behalf of connection %s",
                arguments.connection().id());

        ServiceTarget service = resolveService(mcpResource.getType(), SERVICE_TYPE_RESOURCE_PROVIDER);

        LOG.infof("Requesting %s from %s", mcpResource.getName(), service.toAddress());

        ResourceRequest request = buildResourceRequest(mcpResource);
        ResourceReply reply = transport.acquireResource(request, service);

        return responseTransformer.processReply(reply, arguments, mcpResource);
    }

    // TODO: still blocking
    @Override
    public List<ResourceContents> readAsync(
            ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {

        // This is not performing well ... needs to check
        /*
        return Uni.createFrom()
                .item(() -> doReadAsync(arguments, mcpResource))
                .await()
                .indefinitely();
         */

        final CompletableFuture<List<ResourceContents>> completionStage = Uni.createFrom()
                .item(() -> doReadAsync(arguments, mcpResource))
                //                .runSubscriptionOn(executor)
                .subscribe()
                .asCompletionStage();

        try {
            return completionStage.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ResourceContents> doReadAsync(
            ResourceManager.ResourceArguments arguments, ResourceReference mcpResource) {
        LOG.infof(
                "Asynchronously requesting resource on behalf of connection %s",
                arguments.connection().id());

        ServiceTarget service = resolveService(mcpResource.getType(), SERVICE_TYPE_RESOURCE_PROVIDER);

        LOG.infof("Requesting %s from %s", mcpResource.getName(), service.toAddress());

        ResourceRequest request = buildResourceRequest(mcpResource);

        AsyncTransformer responseTransformer = new AsyncTransformer();

        transport.acquireResourceAsync(
                request,
                service,
                executor,
                reply -> responseTransformer.processReplyAsync(reply, arguments, mcpResource));

        return responseTransformer.getReply();
    }

    /**
     * Builds a resource request from the resource reference.
     *
     * @param mcpResource the resource reference
     * @return the resource request
     */
    private ResourceRequest buildResourceRequest(ResourceReference mcpResource) {
        return ResourceRequest.newBuilder()
                .setLocation(mcpResource.getLocation())
                .setType(mcpResource.getType())
                .setName(mcpResource.getName())
                .setConfigurationUri(Objects.requireNonNullElse(mcpResource.getConfigurationURI(), EMPTY_ARGUMENT))
                .setSecretsUri(Objects.requireNonNullElse(mcpResource.getSecretsURI(), EMPTY_ARGUMENT))
                .build();
    }

    @Override
    public ProvisioningReference provision(ResourcePayload payload) {
        ResourceReference resourceReference = payload.getPayload();

        LOG.debugf("Provisioning resource: %s (type: %s)", resourceReference.getName(), resourceReference.getType());

        ServiceTarget service = resolveService(resourceReference.getType(), SERVICE_TYPE_RESOURCE_PROVIDER);

        return transport.provision(
                resourceReference.getName(), payload.getConfigurationData(), payload.getSecretsData(), service);
    }

    /**
     * Resolves a service target for the specified type and service type.
     * <p>
     * This method uses the service resolver to locate the appropriate service
     * and throws an exception if no service is found.
     *
     * @param type the service type identifier
     * @param serviceType the category of service (e.g., "tool-invoker", "resource-provider")
     * @return the resolved service target
     * @throws ServiceNotFoundException if no service is registered for the given type
     */
    private ServiceTarget resolveService(String type, String serviceType) {
        LOG.debugf("Resolving service for type '%s' and service type '%s'", type, serviceType);
        ServiceTarget service = serviceResolver.resolve(type, serviceType);
        if (service == null) {
            throw new ServiceNotFoundException("There is no host registered for service " + type);
        }
        LOG.debugf("Resolved service: %s", service.toAddress());
        return service;
    }
}
