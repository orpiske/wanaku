package ai.wanaku.backend.proxies;

import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.exceptions.ServiceNotFoundException;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.PayloadType;
import ai.wanaku.core.exchange.Secret;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Objects;

/**
 * Abstract base class for gRPC-based proxies. Provides common infrastructure
 * for provisioning configurations and secrets via gRPC services.
 */
public abstract class AbstractGrpcProxy implements Proxy {
    protected final ServiceResolver serviceResolver;

    protected AbstractGrpcProxy(ServiceResolver serviceResolver) {
        this.serviceResolver = serviceResolver;
    }

    /**
     * Performs the provisioning operation for a given reference.
     * This method encapsulates the common logic for building Configuration and Secret
     * objects and delegating to the gRPC provisioner service.
     *
     * @param referenceName    the name of the reference being provisioned
     * @param referenceType    the type of the reference (used for service resolution)
     * @param configData       the configuration data (nullable)
     * @param secretsData      the secrets data (nullable)
     * @param serviceType      the type of service to resolve
     * @return A provisioning reference containing URIs and property schemas
     * @throws ServiceNotFoundException if no service is registered for the reference type
     */
    protected ProvisioningReference performProvisioning(
            String referenceName,
            String referenceType,
            String configData,
            String secretsData,
            ServiceType serviceType) {

        ServiceTarget service = serviceResolver.resolve(referenceType, serviceType);
        if (service == null) {
            throw new ServiceNotFoundException("There is no host registered for service " + referenceType);
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(service.toAddress())
                .usePlaintext()
                .build();

        final Configuration cfg = Configuration.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(referenceName)
                .setPayload(Objects.requireNonNullElse(configData, ""))
                .build();

        final Secret secret = Secret.newBuilder()
                .setType(PayloadType.BUILTIN)
                .setName(referenceName)
                .setPayload(Objects.requireNonNullElse(secretsData, ""))
                .build();

        return ProxyHelper.provision(cfg, secret, channel, service);
    }
}
