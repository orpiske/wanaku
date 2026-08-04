package ai.wanaku.backend.api.v1.tools;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import org.jboss.logging.Logger;
import ai.wanaku.backend.common.LabelsAwareWanakuEntityBean;
import ai.wanaku.backend.core.persistence.api.ToolReferenceRepository;
import ai.wanaku.backend.core.persistence.api.WanakuRepository;
import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;

@ApplicationScoped
public class ToolsBean extends LabelsAwareWanakuEntityBean<ToolReference> {
    private static final Logger LOG = Logger.getLogger(ToolsBean.class);

    @Inject
    Instance<ToolReferenceRepository> toolReferenceRepositoryInstance;

    private ToolReferenceRepository toolReferenceRepository;

    @PostConstruct
    void init() {
        toolReferenceRepository = toolReferenceRepositoryInstance.get();
    }

    public ToolReference add(ToolReference toolReference) {
        return toolReferenceRepository.persist(toolReference);
    }

    public List<ToolReference> list() {
        return toolReferenceRepository.listAll();
    }

    public List<ToolReference> list(String labelFilter) {
        if (labelFilter == null || labelFilter.isBlank()) {
            return toolReferenceRepository.listAll();
        }
        return toolReferenceRepository.findAllFilterByLabelExpression(labelFilter);
    }

    public int remove(String name) throws WanakuException {
        return removeByName(name);
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
