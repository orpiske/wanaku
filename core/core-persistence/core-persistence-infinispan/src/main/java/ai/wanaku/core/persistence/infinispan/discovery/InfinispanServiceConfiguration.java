package ai.wanaku.core.persistence.infinispan.discovery;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import ai.wanaku.core.mcp.providers.ServiceRegistry;
import io.quarkus.arc.lookup.LookupIfProperty;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.manager.EmbeddedCacheManager;

public class InfinispanServiceConfiguration {
    @Inject
    EmbeddedCacheManager cacheManager;

    @Inject
    Configuration configuration;

    @Inject
    Instance<InfinispanResourceTargetRepository> targetRepositoryInstance;

    @Inject
    Instance<InfinispanToolTargetRepository> toolTargetRepositoryInstance;

    @Produces
    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    ServiceRegistry serviceRegistry() {
        return new InfinispanServiceRegistry(targetRepositoryInstance.get(), toolTargetRepositoryInstance.get());
    }
}
