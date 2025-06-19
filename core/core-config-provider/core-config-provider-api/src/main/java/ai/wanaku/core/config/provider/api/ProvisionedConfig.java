package ai.wanaku.core.config.provider.api;

import java.net.URI;

public record ProvisionedConfig(URI configurationsUri, URI secretsUri) {
}
