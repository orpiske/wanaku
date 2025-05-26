package ai.wanaku.core.persistence.infinispan;

import ai.wanaku.api.types.ForwardReference;
import ai.wanaku.core.persistence.api.ForwardReferenceRepository;
import ai.wanaku.core.persistence.types.ForwardEntity;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanForwardReferenceRepository extends AbstractInfinispanRepository<ForwardReference, ForwardEntity, String> implements
        ForwardReferenceRepository {

    public InfinispanForwardReferenceRepository(EmbeddedCacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String entityName() {
        return "forward";
    }
}
