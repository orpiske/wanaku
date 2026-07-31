package ai.wanaku.backend.api.v1.tools;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import org.jboss.logging.Logger;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkus.runtime.StartupEvent;
import ai.wanaku.backend.api.v1.namespaces.NamespacesBean;
import ai.wanaku.backend.bridge.ToolsBridge;
import ai.wanaku.backend.common.LabelsAwareWanakuEntityBean;
import ai.wanaku.backend.common.ToolsHelper;
import ai.wanaku.backend.core.persistence.api.ToolReferenceRepository;
import ai.wanaku.backend.core.persistence.api.WanakuRepository;
import ai.wanaku.capabilities.sdk.api.exceptions.EntityAlreadyExistsException;
import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.Namespace;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;
import ai.wanaku.core.util.StringHelper;

@ApplicationScoped
public class ToolsBean extends LabelsAwareWanakuEntityBean<ToolReference> {
    private static final Logger LOG = Logger.getLogger(ToolsBean.class);

    @Inject
    ToolManager toolManager;

    @Inject
    ToolsBridge toolsBridge;

    @Inject
    NamespacesBean namespacesBean;

    @Inject
    Instance<ToolReferenceRepository> toolReferenceRepositoryInstance;

    private ToolReferenceRepository toolReferenceRepository;

    @PostConstruct
    void init() {
        toolReferenceRepository = toolReferenceRepositoryInstance.get();
    }

    public ToolReference add(ToolReference toolReference) {
        // then registers the tool with the tool manager
        registerTool(toolReference);

        // if all goes well, persist the tool, so it can be loaded back when restarting
        return toolReferenceRepository.persist(toolReference);
    }

    private void registerTool(ToolReference toolReference) {
        if (!StringHelper.isEmpty(toolReference.getNamespace())) {
            final Namespace namespace = namespacesBean.alocateNamespace(toolReference.getNamespace());
            ToolsHelper.registerTool(toolReference, toolManager, namespace, toolsBridge::execute);
        } else {
            ToolsHelper.registerTool(toolReference, toolManager, toolsBridge::execute);
        }
    }

    public List<ToolReference> list() {
        return toolReferenceRepository.listAll();
    }

    public List<ToolReference> list(String labelFilter) {
        if (StringHelper.isBlank(labelFilter)) {
            return toolReferenceRepository.listAll();
        }
        return toolReferenceRepository.findAllFilterByLabelExpression(labelFilter);
    }

    void loadTools(@Observes StartupEvent ev) {
        // Preload data
        namespacesBean.preload();

        for (ToolReference toolReference : list()) {
            try {
                registerTool(toolReference);
            } catch (EntityAlreadyExistsException e) {
                LOG.errorf(
                        e,
                        "Error registering a tool named %s during startup, but it already exists",
                        toolReference.getName());
            }
        }
    }

    public int remove(String name) throws WanakuException {
        int removed = 0;
        try {
            removed = removeByName(name);
        } finally {
            if (removed > 0) {
                toolManager.removeTool(name);
            }
        }

        return removed;
    }

    public void update(ToolReference resource) {
        toolReferenceRepository.update(resource.getId(), resource);
    }

    public ToolReference getByName(String name) {
        List<ToolReference> tools = toolReferenceRepository.findByName(name);
        return tools.isEmpty() ? null : tools.getFirst();
    }

    @Override
    protected WanakuRepository<ToolReference, String> getRepository() {
        return toolReferenceRepository;
    }

    @Override
    public int removeIf(String labelExpression) throws WanakuException {
        List<ToolReference> toRemove = list(labelExpression);
        toRemove.stream().map(t -> t.getName()).forEach(this::remove);
        return toRemove.size();
    }
}
