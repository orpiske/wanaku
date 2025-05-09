package ai.wanaku.routers.config;

import ai.wanaku.core.config.WanakuConfig;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Base configuration class for the router
 */
@ConfigMapping(prefix = "wanaku.router")
public interface WanakuRouterConfig extends WanakuConfig {

    @WithDefault("${user.home}/.wanaku/router/")
    String indexesPath();
}
