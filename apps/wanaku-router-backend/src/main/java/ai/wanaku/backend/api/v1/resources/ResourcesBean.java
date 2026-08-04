package ai.wanaku.backend.api.v1.resources;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import org.jboss.logging.Logger;
import ai.wanaku.backend.common.AbstractBean;
import ai.wanaku.backend.core.persistence.api.ResourceReferenceRepository;
import ai.wanaku.backend.core.persistence.api.WanakuRepository;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;

@ApplicationScoped
public class ResourcesBean extends AbstractBean<ResourceReference> {
    private static final Logger LOG = Logger.getLogger(ResourcesBean.class);

    @Inject
    Instance<ResourceReferenceRepository> resourceReferenceRepositoryInstance;

    private ResourceReferenceRepository resourceReferenceRepository;

    @PostConstruct
    public void init() {
        resourceReferenceRepository = resourceReferenceRepositoryInstance.get();
    }

    public ResourceReference expose(ResourceReference mcpResource) {
        return resourceReferenceRepository.persist(mcpResource);
    }

    public List<ResourceReference> list(String labelFilter) {
        if (labelFilter == null || labelFilter.isBlank()) {
            return resourceReferenceRepository.listAll();
        }
        return resourceReferenceRepository.findAllFilterByLabelExpression(labelFilter);
    }

    public List<ResourceReference> list() {
        return list(null);
    }

    public int remove(String name) {
        return resourceReferenceRepository.removeByField("name", name);
    }

    public ResourceReference getByName(String name) {
        List<ResourceReference> resources = resourceReferenceRepository.findByName(name);
        return resources.isEmpty() ? null : resources.getFirst();
    }

    public void update(ResourceReference resource) {
        resourceReferenceRepository.update(resource.getId(), resource);
    }

    @Override
    protected WanakuRepository<ResourceReference, String> getRepository() {
        return resourceReferenceRepository;
    }
}
