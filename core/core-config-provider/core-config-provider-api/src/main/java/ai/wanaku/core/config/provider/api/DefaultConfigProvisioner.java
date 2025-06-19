package ai.wanaku.core.config.provider.api;

import java.net.URI;

public class DefaultConfigProvisioner implements ConfigProvisioner {
    private final ConfigWriter configWriter;
    private final SecretWriter secretWriter;

    public DefaultConfigProvisioner(ConfigWriter configWriter, SecretWriter secretWriter) {
        this.configWriter = configWriter;
        this.secretWriter = secretWriter;
    }

    @Override
    public URI provisionConfiguration(String id, String data) {
        return configWriter.write(id, data);
    }

    @Override
    public URI provisionSecret(String id, String data) {
        return secretWriter.write(id, data);
    }


}
