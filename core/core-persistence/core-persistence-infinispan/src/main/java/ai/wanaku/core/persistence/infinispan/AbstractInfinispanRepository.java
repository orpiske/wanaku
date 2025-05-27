package ai.wanaku.core.persistence.infinispan;

import jakarta.inject.Inject;

import ai.wanaku.core.persistence.api.WanakuRepository;
import ai.wanaku.core.persistence.types.WanakuEntity;
import java.util.List;
import org.infinispan.Cache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.SingleFileStoreConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.MessageMarshaller;

public abstract class AbstractInfinispanRepository<A, T extends WanakuEntity, K> implements WanakuRepository<A, T, K> {

    protected final EmbeddedCacheManager cacheManager;

    protected AbstractInfinispanRepository(EmbeddedCacheManager cacheManager) {
        this.cacheManager = cacheManager;

        configure();
    }

    @Override
    public void persist(A model) {
        final Cache<Object, T> cache = cacheManager.getCache(entityName());

        T entity = convertToEntity(model);

        cache.put(entity.getId(), entity);
    }

    @Override
    public List<A> listAll() {
        final Cache<Object, T> cache = cacheManager.getCache(entityName());
        return convertToModels(cache.values().stream().toList());
    }

    @Override
    public boolean deleteById(K id) {
        final Cache<Object, T> cache = cacheManager.getCache(entityName());

        if (cache.remove(id) != null) {
            return true;
        }

        return false;
    }

    @Override
    public A findById(K id) {
        final Cache<Object, T> cache = cacheManager.getCache(entityName());
        return convertToModel(cache.get(id));
    }

    @Override
    public boolean update(K id, A model) {
        final Cache<Object, T> cache = cacheManager.getCache(entityName());

        if (cache.put(id, convertToEntity(model)) != null) {
            return true;
        }

        return false;
    }

    protected abstract String entityName();

    protected void configure() {
        final Configuration config = new ConfigurationBuilder()
                .persistence()
                .passivation(false)
                .addStore(SingleFileStoreConfigurationBuilder.class)
                .location("/Users/opiske/tmp/infi")
                .encoding().key().mediaType(MediaType.APPLICATION_PROTOSTREAM_TYPE)
                .encoding().value().mediaType(MediaType.APPLICATION_PROTOSTREAM_TYPE)
//                .encoding().key().mediaType(MediaType.APPLICATION_OBJECT_TYPE)
//                .encoding().value().mediaType(MediaType.APPLICATION_OBJECT_TYPE)
                .build();
        cacheManager.defineConfiguration(entityName(), config);
    }
}
