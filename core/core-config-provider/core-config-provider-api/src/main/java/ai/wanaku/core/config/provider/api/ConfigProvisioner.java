package ai.wanaku.core.config.provider.api;

import java.net.URI;

public interface ConfigProvisioner {

    URI provisionConfiguration(String id, String data);
    URI provisionSecret(String id, String data);
}
