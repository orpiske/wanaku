package ai.wanaku.core.mcp.providers;

import ai.wanaku.api.types.management.Service;
import ai.wanaku.api.types.management.State;

import java.util.List;

/**
 * Defines a registry of downstream services
 */
public interface ServiceRegistry {

    /**
     * Register a service target in the registry
     * @param serviceTarget the service target
     */
    void register(ServiceTarget serviceTarget);

    /**
     * De-register a service from the registry
     * @param serviceTarget the service target
     */
    void deregister(ServiceTarget serviceTarget);

    /**
     * Gets a registered service by name
     * @param service the name of the service
     * @param serviceType the service type
     * @return the service instance or null if not found
     */
    Service getService(String service, ServiceType serviceType);

    /**
     * Saves the current state of the service
     * @param service the service to save the state
     * @param healthy whether it is healthy (true for healthy, false otherwise)
     * @param message Optional state message (ignored if healthy)
     */
    @Deprecated
    void saveState(String service, boolean healthy, String message);

    /**
     * Gets the state of the given service
     * @param service the service name
     * @param count the number of states to get
     * @return the last count states of the given service
     */
    List<State> getState(String service, int count);

    /**
     * Get a map of all registered services and their configurations
     *
     * @param serviceType the type of service to get
     * @return a map of all registered services and their configurations
     */
    List<ServiceTarget> getEntries(ServiceType serviceType);


    /**
     * Update a registered service target in the registry
     * @param serviceTarget the service target
     */
    void update(ServiceTarget serviceTarget);

    void update(String target, String option, String value);
}
