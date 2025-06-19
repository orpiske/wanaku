package ai.wanaku.core.runtime.camel;

import ai.wanaku.core.config.provider.api.ConfigResource;
import java.util.Map;
import java.util.stream.Collectors;

public class CamelQueryParameterBuilder {
    private final ConfigResource configResource;

    public CamelQueryParameterBuilder(ConfigResource configResource) {
        this.configResource = configResource;
    }

    public Map<String, String> build() {
        final Map<String, String> configs = configResource.getConfigs();

        final Map<String, String> secrets = configResource.getSecrets().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.setValue(String.format("RAW(%s)", e.getValue()))));

        configs.putAll(secrets);
        return configs;
    }

    public static Map<String, String> build(ConfigResource configResource) {
        final CamelQueryParameterBuilder queryParameterBuilder = new CamelQueryParameterBuilder(configResource);
        return queryParameterBuilder.build();
    }
}
