package ai.wanaku.backend.proxies;

import ai.wanaku.backend.support.ProvisioningReference;

/**
 * Generic interface for components that can be provisioned with configuration and secrets.
 *
 * @param <P> the payload type containing provisioning data
 */
public interface Provisionable<P> {
    /**
     * Provision a configuration in the service
     *
     * @param payload the payload to provision in the service
     * @return A provisioning reference instance containing URIs and property schemas
     */
    ProvisioningReference provision(P payload);
}
