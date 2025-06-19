package ai.wanaku.core.capabilities.common;

import ai.wanaku.core.capabilities.config.WanakuServiceConfig;
import ai.wanaku.core.config.provider.api.ConfigProvisioner;
import ai.wanaku.core.config.provider.api.ConfigWriter;
import ai.wanaku.core.config.provider.api.DefaultConfigProvisioner;
import ai.wanaku.core.config.provider.api.ProvisionedConfig;
import ai.wanaku.core.config.provider.api.SecretWriter;
import ai.wanaku.core.config.provider.file.FileConfigurationWriter;
import ai.wanaku.core.config.provider.file.FileSecretWriter;
import ai.wanaku.core.exchange.Configuration;
import ai.wanaku.core.exchange.PayloadType;
import ai.wanaku.core.exchange.ProvisionRequest;
import ai.wanaku.core.exchange.Secret;
import java.net.URI;

public final class ConfigProvisionerLoader {

    private ConfigProvisionerLoader() {}

    public static ConfigProvisioner newConfigProvisioner(ProvisionRequest request, WanakuServiceConfig config) {
        final Configuration configuration = request.getConfiguration();
        final Secret secret = request.getSecret();
        ConfigWriter configurationWriter;
        SecretWriter secretWriter;

        if (configuration.getType() == PayloadType.BUILTIN) {
            final String serviceHome = ServicesHelper.getCanonicalServiceHome(config);

            configurationWriter = new FileConfigurationWriter(serviceHome);
        } else {
            throw new UnsupportedOperationException("Provisioner not supported yet.");
        }

        if (secret.getType() == PayloadType.BUILTIN) {
            final String serviceHome = ServicesHelper.getCanonicalServiceHome(config);

            secretWriter = new FileSecretWriter(serviceHome);
        } else {
            throw new UnsupportedOperationException("Provisioner not supported yet.");
        }

        return new DefaultConfigProvisioner(configurationWriter, secretWriter);
    }

    public static ProvisionedConfig provision(ProvisionRequest request, ConfigProvisioner provisioner) {
        final Configuration configuration = request.getConfiguration();
        final Secret secret = request.getSecret();

        final URI configurationUri = provisioner.provisionConfiguration(request.getUri(), configuration.getPayload());
        final URI secretUri = provisioner.provisionSecret(request.getUri(), secret.getPayload());

        return new ProvisionedConfig(configurationUri, secretUri);
    }
}
