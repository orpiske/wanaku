package ai.wanaku.core.services.discovery;

import ai.wanaku.api.types.providers.ServiceTarget;
import ai.wanaku.api.types.providers.ServiceType;

public interface RegistrationManager {

    void register();
    void deregister();
    void ping();
    void lastAsFail(String reason);
    void lastAsSuccessful();

}
