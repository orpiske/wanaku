package ai.wanaku.backend.api.v1.management.statistics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import ai.wanaku.backend.api.v1.capabilities.CapabilitiesBean;
import ai.wanaku.backend.api.v1.datastores.DataStoresBean;
import ai.wanaku.backend.api.v1.servicecatalog.ServiceCatalogBean;
import ai.wanaku.backend.api.v1.servicecatalog.ServiceTemplateBean;
import ai.wanaku.backend.api.v1.toolsetrepos.ToolsetReposBean;
import ai.wanaku.capabilities.sdk.api.types.discovery.ActivityRecord;
import ai.wanaku.capabilities.sdk.api.types.discovery.HealthStatus;

@ApplicationScoped
public class StatisticsBean {

    @Inject
    ServiceCatalogBean serviceCatalogBean;

    @Inject
    ServiceTemplateBean serviceTemplateBean;

    @Inject
    ToolsetReposBean toolsetReposBean;

    @Inject
    DataStoresBean dataStoresBean;

    @Inject
    CapabilitiesBean capabilitiesBean;

    public SystemStatistics getStatistics() {
        long serviceCatalogsCount = serviceCatalogBean.list(null).size();
        long serviceTemplatesCount = serviceTemplateBean.list(null).size();
        long toolsetReposCount = toolsetReposBean.list().size();
        long dataStoresCount = dataStoresBean.list(null).size();

        CapabilityStatistics toolCapabilities = buildCapabilityStatistics(capabilitiesBean.toolsState());
        CapabilityStatistics resourceCapabilities = buildCapabilityStatistics(capabilitiesBean.resourcesState());

        return new SystemStatistics(
                serviceCatalogsCount,
                serviceTemplatesCount,
                toolsetReposCount,
                dataStoresCount,
                toolCapabilities,
                resourceCapabilities);
    }

    private CapabilityStatistics buildCapabilityStatistics(Map<String, List<ActivityRecord>> stateMap) {
        long healthy = 0;
        long unhealthy = 0;
        long down = 0;
        long pending = 0;

        for (List<ActivityRecord> records : stateMap.values()) {
            for (ActivityRecord record : records) {
                HealthStatus status = record.getHealthStatus();
                switch (status) {
                    case HEALTHY -> healthy++;
                    case UNHEALTHY -> unhealthy++;
                    case DOWN -> down++;
                    case PENDING -> pending++;
                }
            }
        }

        return new CapabilityStatistics(healthy + unhealthy + down + pending, healthy, unhealthy, down, pending);
    }
}
