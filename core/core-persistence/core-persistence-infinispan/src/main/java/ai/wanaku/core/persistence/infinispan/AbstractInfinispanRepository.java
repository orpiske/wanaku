package ai.wanaku.core.persistence.infinispan;

import ai.wanaku.core.persistence.api.WanakuRepository;
import ai.wanaku.core.persistence.types.WanakuEntity;
import java.util.List;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;

public abstract class AbstractInfinispanRepository<A, T extends WanakuEntity, K> implements WanakuRepository<A, T, K> {

    protected final EmbeddedCacheManager cacheManager;

    protected AbstractInfinispanRepository(EmbeddedCacheManager cacheManager) {
        this.cacheManager = cacheManager;

        configure();
    }

    @Override
    public void persist(A model) {
        final Cache<Object, A> cache = cacheManager.getCache(entityName());

        T entity = convertToEntity(model);

        cache.put(entity.getId(), model);
    }

    @Override
    public List<A> listAll() {
        final Cache<Object, A> cache = cacheManager.getCache(entityName());
        return cache.values().stream().toList();
    }

    @Override
    public boolean deleteById(K id) {
        final Cache<Object, A> cache = cacheManager.getCache(entityName());

        if (cache.remove(id) != null) {
            return true;
        }

        return false;
    }

    @Override
    public A findById(K id) {
        final Cache<Object, A> cache = cacheManager.getCache(entityName());
        return cache.get(id);
    }

    @Override
    public boolean update(K id, A model) {
        final Cache<Object, A> cache = cacheManager.getCache(entityName());

        if (cache.put(id, model) != null) {
            return true;
        }

        return false;
    }

    protected abstract String entityName();

    protected void configure() {
        final Configuration config = new ConfigurationBuilder().build();
        cacheManager.defineConfiguration(entityName(), config);
    }
}
