package ai.wanaku.core.persistence.infinispan;

import ai.wanaku.api.types.ToolReference;
import ai.wanaku.core.persistence.api.ToolReferenceRepository;
import ai.wanaku.core.persistence.types.ToolReferenceEntity;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanToolReferenceRepository extends AbstractInfinispanRepository<ToolReference, ToolReferenceEntity, String> implements
        ToolReferenceRepository {

    public InfinispanToolReferenceRepository(EmbeddedCacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected String entityName() {
        return "tool";
    }
}
