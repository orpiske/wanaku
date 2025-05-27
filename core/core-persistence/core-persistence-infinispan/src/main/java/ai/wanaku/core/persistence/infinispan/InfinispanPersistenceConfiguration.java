package ai.wanaku.core.persistence.infinispan;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import ai.wanaku.core.persistence.api.ForwardReferenceRepository;
import ai.wanaku.core.persistence.api.ResourceReferenceRepository;
import ai.wanaku.core.persistence.api.ToolReferenceRepository;
import ai.wanaku.core.persistence.infinispan.marshaller.ForwardMarshaller;
import ai.wanaku.core.persistence.infinispan.marshaller.ResourceMarshaller;
import ai.wanaku.core.persistence.infinispan.marshaller.ToolReferenceEntityMarshaller;
import io.quarkus.arc.lookup.LookupIfProperty;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.protostream.MessageMarshaller;

public class InfinispanPersistenceConfiguration {

    @Inject
    EmbeddedCacheManager cacheManager;

    @Produces
    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    ResourceReferenceRepository resourceReferenceRepository() {
        return new InfinispanResourceReferenceRepository(cacheManager);
    }

    @Produces
    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    ToolReferenceRepository toolReferenceRepository() {
        return new InfinispanToolReferenceRepository(cacheManager);
    }

    @Produces
    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    ForwardReferenceRepository forwardReferenceRepository() {
        return new InfinispanForwardReferenceRepository(cacheManager);
    }


}
