package ai.wanaku.server.quarkus.api.v1.management.targets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ai.wanaku.api.exceptions.ConfigurationNotFoundException;
import ai.wanaku.api.exceptions.ServiceNotFoundException;
import ai.wanaku.api.types.management.Service;
import ai.wanaku.core.mcp.common.resolvers.ResourceResolver;
import ai.wanaku.core.mcp.common.resolvers.ToolsResolver;
import ai.wanaku.core.service.discovery.ServiceRegistry;
import ai.wanaku.core.util.IndexHelper;
import java.io.IOException;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TargetsBean {
    private static final Logger LOG = Logger.getLogger(TargetsBean.class);

    @Inject
    ResourceResolver resourceResolver;

    @Inject
    ToolsResolver toolsResolver;

    @Inject
    ServiceRegistry serviceRegistry;

    public void configureTools(String service, String option, String value)
            throws IOException, ConfigurationNotFoundException, ServiceNotFoundException {
        Map<String, String> configurations = toolsConfigurations(service);
        configurations.put(option, value);

        IndexHelper.saveTargetsIndex(toolsResolver.targetsIndexFile(), configurations);
    }

    public void configureResources(String service, String option, String value)
            throws IOException, ConfigurationNotFoundException, ServiceNotFoundException {
        Map<String, String> configurations = resourcesConfigurations(service);
        configurations.put(option, value);

        IndexHelper.saveTargetsIndex(toolsResolver.targetsIndexFile(), configurations);
    }

    public Map<String,Service> toolList() {
        return serviceRegistry.getEntries();
    }

    public Map<String, Service> resourcesList() {
        return serviceRegistry.getEntries();
    }

    public Map<String, String> toolsConfigurations(String target) {
        Map<String, String> configurations = toolsResolver.getServiceConfigurations(target);
        for (var entry : configurations.entrySet()) {
            LOG.infof("Received tool configuration %s from %s: %s", entry.getKey(), target, entry.getValue());
        }
        return configurations;
    }

    public Map<String, String> resourcesConfigurations(String target) {
        Map<String, String> configurations = resourceResolver.getServiceConfigurations(target);
        for (var entry : configurations.entrySet()) {
            LOG.infof("Received resource configuration %s from %s: %s", entry.getKey(), target, entry.getValue());
        }
        return configurations;
    }




}
