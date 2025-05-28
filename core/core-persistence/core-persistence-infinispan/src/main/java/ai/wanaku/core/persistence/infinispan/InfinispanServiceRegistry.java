package ai.wanaku.core.persistence.infinispan;

import ai.wanaku.api.types.management.Configurations;
import ai.wanaku.api.types.management.Service;
import ai.wanaku.api.types.management.State;
import ai.wanaku.core.mcp.providers.ServiceRegistry;
import ai.wanaku.core.mcp.providers.ServiceTarget;
import ai.wanaku.core.mcp.providers.ServiceType;
import java.util.List;

public class InfinispanServiceRegistry implements ServiceRegistry {

    private final InfinispanToolTargetRepository toolRepository;

    private final InfinispanResourceTargetRepository resourceTargetRepository;

    public InfinispanServiceRegistry(InfinispanResourceTargetRepository resourceTargetRepository, InfinispanToolTargetRepository toolRepository) {
        this.resourceTargetRepository = resourceTargetRepository;
        this.toolRepository = toolRepository;
    }

    @Override
    public void register(ServiceTarget serviceTarget) {
        if (serviceTarget.getServiceType() == ServiceType.TOOL_INVOKER) {
            toolRepository.persist(serviceTarget);
        } else {
            resourceTargetRepository.persist(serviceTarget);
        }
    }

    @Override
    public void deregister(ServiceTarget serviceTarget) {
        if (serviceTarget.getServiceType() == ServiceType.TOOL_INVOKER) {
            toolRepository.deleteById(serviceTarget.getService());
        } else {
            resourceTargetRepository.deleteById(serviceTarget.getService());
        }
    }

    @Override
    public Service getService(String service, ServiceType serviceType) {
        if (serviceType == ServiceType.TOOL_INVOKER) {
            final ServiceTarget target = toolRepository.findById(service);

            return toService(target);
        } else {
            final ServiceTarget target = resourceTargetRepository.findById(service);

            return toService(target);
        }
    }

    @Override
    public void saveState(String service, boolean healthy, String message) {
        // TODO: not yet supported
    }

    @Override
    public List<State> getState(String service, int count) {
        return List.of();
    }

    @Override
    public List<ServiceTarget> getEntries(ServiceType serviceType) {
        if (serviceType == ServiceType.TOOL_INVOKER) {
            return toolRepository.listAll();
        } else {
            return resourceTargetRepository.listAll();
        }
    }

    @Override
    public void update(ServiceTarget serviceTarget) {
        register(serviceTarget);
    }

    @Override
    public void update(String target, String option, String value) {

    }

    private static Service toService(ServiceTarget entity) {
        Service model = new Service();

        Configurations configurations = new Configurations();

        // TODO
        // configurations.setConfigurations(entity.getConfigurations());
        model.setConfigurations(configurations);

        model.setTarget(entity.toAddress());
        return model;
    }

    /**
     * Used for testing
     */
    void clear() {
        resourceTargetRepository.deleteALl();
        toolRepository.deleteALl();
    }
}
