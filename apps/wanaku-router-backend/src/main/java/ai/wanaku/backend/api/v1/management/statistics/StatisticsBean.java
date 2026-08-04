package ai.wanaku.backend.api.v1.management.statistics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import ai.wanaku.backend.api.v1.capabilities.CapabilitiesBean;
import ai.wanaku.backend.api.v1.datastores.DataStoresBean;
import ai.wanaku.capabilities.sdk.api.types.discovery.ActivityRecord;
import ai.wanaku.capabilities.sdk.api.types.discovery.HealthStatus;

@ApplicationScoped
public class StatisticsBean {

    @Inject
    DataStoresBean dataStoresBean;

    @Inject
    CapabilitiesBean capabilitiesBean;

    public SystemStatistics getStatistics() {
        long toolsCount = 0;
        long resourcesCount = 0;
        long promptsCount = 0;
        long forwardsCount = 0;
        long dataStoresCount = dataStoresBean.list(null).size();

        CapabilityStatistics toolCapabilities = buildCapabilityStatistics(capabilitiesBean.toolsState());
        CapabilityStatistics resourceCapabilities = buildCapabilityStatistics(capabilitiesBean.resourcesState());

        return new SystemStatistics(
                toolsCount,
                resourcesCount,
                promptsCount,
                forwardsCount,
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
