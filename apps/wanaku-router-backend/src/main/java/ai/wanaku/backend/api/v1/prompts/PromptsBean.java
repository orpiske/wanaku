package ai.wanaku.backend.api.v1.prompts;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.List;
import org.jboss.logging.Logger;
import ai.wanaku.backend.common.AbstractBean;
import ai.wanaku.backend.core.persistence.api.PromptReferenceRepository;
import ai.wanaku.backend.core.persistence.api.WanakuRepository;
import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.PromptReference;

@ApplicationScoped
public class PromptsBean extends AbstractBean<PromptReference> {
    private static final Logger LOG = Logger.getLogger(PromptsBean.class);

    @Inject
    Instance<PromptReferenceRepository> promptReferenceRepositoryInstance;

    private PromptReferenceRepository promptReferenceRepository;

    @PostConstruct
    void init() {
        promptReferenceRepository = promptReferenceRepositoryInstance.get();
    }

    public PromptReference add(PromptReference promptReference) {
        return promptReferenceRepository.persist(promptReference);
    }

    public List<PromptReference> list() {
        return promptReferenceRepository.listAll();
    }

    public int remove(String name) throws WanakuException {
        return removeByName(name);
    }

    public void update(PromptReference resource) {
        PromptReference existing = getByName(resource.getName());
        if (existing != null) {
            resource.setId(existing.getId());
            promptReferenceRepository.update(resource.getId(), resource);
        }
    }

    public PromptReference getByName(String name) {
        List<PromptReference> prompts = promptReferenceRepository.findByName(name);
        return prompts.isEmpty() ? null : prompts.getFirst();
    }

    @Override
    protected WanakuRepository<PromptReference, String> getRepository() {
        return promptReferenceRepository;
    }
}
