package ai.wanaku.backend.api.v1.management.statistics;

public class SystemStatistics {
    private long serviceCatalogsCount;
    private long serviceTemplatesCount;
    private long toolsetReposCount;
    private long dataStoresCount;
    private CapabilityStatistics toolCapabilities;
    private CapabilityStatistics resourceCapabilities;

    public SystemStatistics() {}

    public SystemStatistics(
            long serviceCatalogsCount,
            long serviceTemplatesCount,
            long toolsetReposCount,
            long dataStoresCount,
            CapabilityStatistics toolCapabilities,
            CapabilityStatistics resourceCapabilities) {
        this.serviceCatalogsCount = serviceCatalogsCount;
        this.serviceTemplatesCount = serviceTemplatesCount;
        this.toolsetReposCount = toolsetReposCount;
        this.dataStoresCount = dataStoresCount;
        this.toolCapabilities = toolCapabilities;
        this.resourceCapabilities = resourceCapabilities;
    }

    public long getServiceCatalogsCount() {
        return serviceCatalogsCount;
    }

    public void setServiceCatalogsCount(long serviceCatalogsCount) {
        this.serviceCatalogsCount = serviceCatalogsCount;
    }

    public long getServiceTemplatesCount() {
        return serviceTemplatesCount;
    }

    public void setServiceTemplatesCount(long serviceTemplatesCount) {
        this.serviceTemplatesCount = serviceTemplatesCount;
    }

    public long getToolsetReposCount() {
        return toolsetReposCount;
    }

    public void setToolsetReposCount(long toolsetReposCount) {
        this.toolsetReposCount = toolsetReposCount;
    }

    public long getDataStoresCount() {
        return dataStoresCount;
    }

    public void setDataStoresCount(long dataStoresCount) {
        this.dataStoresCount = dataStoresCount;
    }

    public CapabilityStatistics getToolCapabilities() {
        return toolCapabilities;
    }

    public void setToolCapabilities(CapabilityStatistics toolCapabilities) {
        this.toolCapabilities = toolCapabilities;
    }

    public CapabilityStatistics getResourceCapabilities() {
        return resourceCapabilities;
    }

    public void setResourceCapabilities(CapabilityStatistics resourceCapabilities) {
        this.resourceCapabilities = resourceCapabilities;
    }
}
