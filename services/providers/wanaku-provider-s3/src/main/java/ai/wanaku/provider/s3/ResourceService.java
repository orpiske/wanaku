package ai.wanaku.provider.s3;

import ai.wanaku.core.exchange.InquireReply;
import ai.wanaku.core.exchange.InquireRequest;
import ai.wanaku.core.exchange.Inquirer;
import ai.wanaku.core.exchange.ResourceAcquirer;
import ai.wanaku.core.exchange.ResourceAcquirerDelegate;
import ai.wanaku.core.exchange.ResourceReply;
import ai.wanaku.core.exchange.ResourceRequest;
import ai.wanaku.core.services.config.WanakuProviderConfig;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@GrpcService
public class ResourceService implements ResourceAcquirer, Inquirer {
    private static final Logger LOG = Logger.getLogger(S3ResourceDelegate.class);

    @Inject
    ResourceAcquirerDelegate delegate;

    @Inject
    WanakuProviderConfig config;

    @ConfigProperty(name = "quarkus.grpc.server.port")
    int port;

    @Override
    public Uni<ResourceReply> resourceAcquire(ResourceRequest request) {
        return Uni.createFrom().item(() -> delegate.acquire(request));
    }

    @Override
    public Uni<InquireReply> inquire(InquireRequest request) {
        InquireReply reply = InquireReply.newBuilder()
                .putAllServiceConfigurations(delegate.serviceConfigurations())
                .putAllCredentialsConfigurations(delegate.credentialsConfigurations())
                .build();

        return Uni.createFrom().item(() -> reply);
    }

    void register(@Observes StartupEvent ev) {
        LOG.info("Registering resource service");

        delegate.register(config.router().address(), config.name(), port);
    }

    void deregister(@Observes ShutdownEvent ev) {
        LOG.info("Deregistering resource service");
        delegate.deregister(config.router().address(), config.name());
    }
}
