package ai.wanaku.core.persistence.infinispan;

import ai.wanaku.api.types.ResourceReference;
import ai.wanaku.core.persistence.api.ResourceReferenceRepository;
import ai.wanaku.core.persistence.types.ResourceReferenceEntity;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanResourceReferenceRepository extends AbstractInfinispanRepository<ResourceReference, ResourceReferenceEntity, String> implements
        ResourceReferenceRepository {

    public InfinispanResourceReferenceRepository(EmbeddedCacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String entityName() {
        return "resource";
    }
}
