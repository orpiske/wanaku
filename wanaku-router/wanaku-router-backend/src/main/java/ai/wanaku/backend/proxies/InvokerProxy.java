package ai.wanaku.backend.proxies;

import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;
import ai.wanaku.capabilities.sdk.api.types.io.ToolPayload;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.PayloadType;
import ai.wanaku.core.exchange.Secret;
import ai.wanaku.core.mcp.common.ToolExecutor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Objects;
import org.jboss.logging.Logger;

/**
 * A proxy class for invoking tools.
 * <p>
 * This proxy is responsible for provisioning tool configurations and
 * providing access to a tool executor. The actual tool execution logic
 * is delegated to {@link InvokerToolExecutor} through composition,
 * separating proxy management from execution concerns.
 */
public class InvokerProxy implements ToolsProxy {
    private static final Logger LOG = Logger.getLogger(InvokerProxy.class);

    private final ServiceResolver serviceResolver;
    private final ToolExecutor executor;

    public InvokerProxy(ServiceResolver serviceResolver) {
        this.serviceResolver = serviceResolver;
        this.executor = new InvokerToolExecutor(serviceResolver);
    }

    @Override
    public ToolExecutor getExecutor() {
        return executor;
    }

    @Override
    public ProvisioningReference provision(ToolPayload toolPayload) {
        ToolReference toolReference = toolPayload.getPayload();

        ServiceTarget service = serviceResolver.resolve(toolReference.getType(), ServiceType.TOOL_INVOKER);
        if (service == null) {
            throw new ServiceNotFoundException("There is no host registered for service " + toolReference.getType());
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(service.toAddress())
                .usePlaintext()
                .build();

        final String configData = Objects.requireNonNullElse(toolPayload.getConfigurationData(), "");
        final Configuration cfg = Configuration.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(toolReference.getName())
                .setPayload(configData)
                .build();

        final String secretsData = Objects.requireNonNullElse(toolPayload.getSecretsData(), "");
        final Secret secret = Secret.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(toolReference.getName())
                .setPayload(secretsData)
                .build();

        return ProxyHelper.provision(cfg, secret, channel, service);
    }

    @Override
    public String name() {
        return "invoker";
    }
}
