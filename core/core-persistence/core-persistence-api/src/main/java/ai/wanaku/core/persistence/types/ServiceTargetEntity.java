package ai.wanaku.core.persistence.types;

import ai.wanaku.core.mcp.providers.ServiceTarget;
import ai.wanaku.core.mcp.providers.ServiceType;
import java.util.Map;

@Deprecated
public class ServiceTargetEntity extends ServiceTarget implements WanakuEntity {


    public ServiceTargetEntity(String service, String host, int port, ServiceType serviceType, Map<String, String> configurations) {
        super(service, host, port, serviceType, configurations);
    }

    // Constructor needed by Jackson
    public ServiceTargetEntity() {
        super(null, null, 0, null, null);
    }

    @Override
    public String getId() {
        return getService();
    }

    @Override
    public void setId(String id) {
        // Do nothing, the Id is mapped via service
    }


}
