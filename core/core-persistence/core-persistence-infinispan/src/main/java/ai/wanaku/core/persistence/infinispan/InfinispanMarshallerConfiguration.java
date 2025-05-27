package ai.wanaku.core.persistence.infinispan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import ai.wanaku.core.persistence.infinispan.marshaller.ForwardMarshaller;
import ai.wanaku.core.persistence.infinispan.marshaller.ResourceMarshaller;
import ai.wanaku.core.persistence.infinispan.marshaller.ToolReferenceEntityMarshaller;
import org.infinispan.protostream.MessageMarshaller;

public class InfinispanMarshallerConfiguration {
    @Produces
        //    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    MessageMarshaller forwardMarshaller() {
        System.out.println("Creating forward marshaller");
        return new ForwardMarshaller();
    }

    @Produces
        //    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    MessageMarshaller resourceMarshaller() {
        System.out.println("Creating resource marshaller");
        return new ResourceMarshaller();
    }

    @Produces
        //    @LookupIfProperty(name = "wanaku.persistence", stringValue = "infinispan")
    MessageMarshaller toolMarshaller() {
        System.out.println("Creating tool marshaller");
        return new ToolReferenceEntityMarshaller();
    }
}
