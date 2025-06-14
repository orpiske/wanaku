package ai.wanaku.server.quarkus.api.v1.forwards;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import ai.wanaku.api.exceptions.WanakuException;
import ai.wanaku.api.types.ForwardReference;
import ai.wanaku.api.types.RemoteToolReference;
import ai.wanaku.api.types.ResourceReference;
import ai.wanaku.api.types.ToolReference;
import ai.wanaku.core.mcp.common.Tool;
import ai.wanaku.core.mcp.common.resolvers.ForwardResolver;
import ai.wanaku.core.mcp.providers.ForwardRegistry;
import ai.wanaku.core.persistence.api.ForwardReferenceRepository;
import ai.wanaku.server.quarkus.common.ResourceHelper;
import ai.wanaku.server.quarkus.common.ToolsHelper;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkus.runtime.StartupEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ForwardsBean {
    private static final Logger LOG = Logger.getLogger(ForwardsBean.class);

    @Inject
    ResourceManager resourceManager;

    @Inject
    ToolManager toolManager;

    @Inject
    ForwardRegistry forwardRegistry;

    @Inject
    Instance<ForwardReferenceRepository> forwardReferenceRepositoryInstance;

    private ForwardReferenceRepository forwardReferenceRepository;

    @PostConstruct
    void init() {
        forwardReferenceRepository = forwardReferenceRepositoryInstance.get();
    }

    public boolean removeReference(ForwardReference matching, ForwardReference test) {
        if (matching.getName().equals(test.getName())) {
            ForwardResolver forwardResolver = forwardRegistry.getResolver(test);
            if (forwardResolver != null) {
                try {
                    removeRemoteTools(forwardResolver);
                    removeRemoteResources(forwardResolver);
                } finally {
                    forwardRegistry.unlink(test);
                }
                return true;
            }
        }

        return false;
    }

    public void remove(ForwardReference reference) {
        if (!forwardReferenceRepository.remove(r -> removeReference(reference, r))) {
            LOG.warnf("No references named %s where found", reference.getName());
        }
    }

    private void removeRemoteResources(ForwardResolver forwardResolver) {
        final List<ResourceReference> resourceReferences = forwardResolver.listResources();
        for (ResourceReference remoteResource : resourceReferences) {
            LOG.infof("Removing remote resource %s", remoteResource);
            try {
                resourceManager.removeResource(remoteResource.getLocation());
            } catch (Exception e) {
                LOG.infof("Failed to remove forward tool %s (server restart may be necessary)",
                        remoteResource.getName());
            }
        }
    }

    private void removeRemoteTools(ForwardResolver forwardResolver) {
        List<RemoteToolReference> remoteToolReferences = forwardResolver.listTools();
        for (RemoteToolReference remoteToolReference : remoteToolReferences) {
            LOG.infof("Removing remote tool %s", remoteToolReference);
            try {
                toolManager.removeTool(remoteToolReference.getName());
            } catch (Exception e) {
                LOG.infof("Failed to remove forward tool %s (server restart may be necessary)",
                        remoteToolReference.getName());
            }
        }
    }

    public void forward(ForwardReference forwardReference) {
        try {
            ForwardReference byId = forwardReferenceRepository.findById(forwardReference.getName());
            if (byId != null) {
                LOG.infof("Forward reference %s already exists", forwardReference);
                return;
            }

            registerForward(forwardReference);

            forwardReferenceRepository.persist(forwardReference);
        } catch (WanakuException e) {
            throw e;
        } catch (Exception e) {
            throw new WanakuException(e);
        }
    }

    private void registerForward(ForwardReference forwardReference) {
        ForwardResolver forwardResolver = forwardRegistry.newResolverForService(forwardReference);

        List<ResourceReference> resourceReferences = forwardResolver.listResources();
        for (ResourceReference reference : resourceReferences) {
            LOG.infof("Exposing remote resource %s", reference.getName());
            ResourceHelper.expose(reference, resourceManager, forwardResolver::read);
        }

        List<RemoteToolReference> toolReferences = forwardResolver.listTools();
        for (RemoteToolReference reference : toolReferences) {
            LOG.infof("Binding remote tool %s", reference.getName());
            Tool tool = forwardResolver.resolve(reference);
            ToolsHelper.registerTool(reference, toolManager, tool::call);
        }

        forwardRegistry.link(forwardReference, forwardResolver);
    }

    public List<ResourceReference> listAllResources() {
        List<ResourceReference> references = new ArrayList<>();
        for (ForwardReference service : forwardRegistry.services()) {
            ForwardResolver forwardResolver = forwardRegistry.getResolver(service);

            List<ResourceReference> remoteToolReferences = forwardResolver.listResources();
            references.addAll(remoteToolReferences);
        }

        return references;
    }

    /**
     * List all tools from forward services
     * @return
     */
    public List<RemoteToolReference> listAllTools() {
        List<RemoteToolReference> references = new ArrayList<>();
        for (ForwardReference service : forwardRegistry.services()) {
            ForwardResolver forwardResolver = forwardRegistry.getResolver(service);

            List<RemoteToolReference> remoteToolReferences = forwardResolver.listTools();
            references.addAll(remoteToolReferences);
        }

        return references;
    }

    public List<ToolReference> listAllAsTools() {
        return listAllTools().stream().map(RemoteToolReference::asToolReference)
                        .collect(Collectors.toList());
    }

    void loadForwards(@Observes StartupEvent event) {
        for (ForwardReference forwardReference : forwardReferenceRepository.listAll()) {
            try {
                registerForward(forwardReference);
            } catch (Exception e) {
                LOG.errorf("Error registering forwards from %s during startup", forwardReference.getAddress());
            }
        }
    }

    public List<ForwardReference> listForwards() {
        Set<ForwardReference> services = forwardRegistry.services();

        return services.stream().toList();
    }

    public void update(ForwardReference resource) {
        forwardReferenceRepository.update(resource.getName(), resource);
    }
}
