package ai.wanaku.routing.yaml.route.service;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import ai.wanaku.core.exchange.InquireReply;
import ai.wanaku.core.exchange.InquireRequest;
import ai.wanaku.core.exchange.Inquirer;
import ai.wanaku.core.exchange.InvocationDelegate;
import ai.wanaku.core.exchange.ToolInvokeReply;
import ai.wanaku.core.exchange.ToolInvokeRequest;
import ai.wanaku.core.exchange.ToolInvoker;
import ai.wanaku.core.services.config.WanakuRoutingConfig;
import io.quarkus.grpc.GrpcService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@GrpcService
public class InvocationService implements ToolInvoker, Inquirer {
    private static final Logger LOG = Logger.getLogger(InvocationService.class);

    @Inject
    InvocationDelegate delegate;

    @Inject
    WanakuRoutingConfig config;

    @ConfigProperty(name = "quarkus.grpc.server.port")
    int port;

    @Override
    public Uni<ToolInvokeReply> invokeTool(ToolInvokeRequest request) {
        return Uni.createFrom().item(() -> delegate.invoke(request));
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
