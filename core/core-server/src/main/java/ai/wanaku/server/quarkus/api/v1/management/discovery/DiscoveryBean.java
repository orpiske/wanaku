package ai.wanaku.server.quarkus.api.v1.management.discovery;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import ai.wanaku.api.types.management.State;
import ai.wanaku.core.mcp.providers.ServiceRegistry;
import ai.wanaku.api.types.providers.ServiceTarget;
import ai.wanaku.api.types.providers.ServiceType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DiscoveryBean {
    private static final Logger LOG = Logger.getLogger(DiscoveryBean.class);

    @Inject
    Instance<ServiceRegistry> serviceRegistryInstance;

    private ServiceRegistry serviceRegistry;

    @PostConstruct
    public void init() {
        serviceRegistry = serviceRegistryInstance.get();
        LOG.info("Using service registry implementation " + serviceRegistry.getClass().getName());
    }

    private List<ServiceTarget> toolList() {
        return serviceRegistry.getEntries(ServiceType.TOOL_INVOKER);
    }

    private List<ServiceTarget> resourcesList() {
        return serviceRegistry.getEntries(ServiceType.RESOURCE_PROVIDER);
    }

    public Map<String, List<State>> toolsState() {
        Map<String, List<State>> states = new HashMap<>();
        List<ServiceTarget> toolsServices = toolList();
        buildState(toolsServices, states);

        return states;
    }

    public Map<String, List<State>> resourcesState() {
        Map<String, List<State>> states = new HashMap<>();

        List<ServiceTarget> resourcesServices = resourcesList();
        buildState(resourcesServices, states);

        return states;
    }

    private void buildState(List<ServiceTarget> serviceTargets, Map<String, List<State>> states) {
        for (var entry : serviceTargets) {
// TODO
//            List<State> state = serviceRegistry.getState(entry.getKey(), 10);

//            states.put(entry.getKey(), state);
        }
    }

    public void registerService(ServiceTarget target) {
        serviceRegistry.register(target);
    }

    public void deregisterService(ServiceTarget target) {
        serviceRegistry.deregister(target);
    }
}
