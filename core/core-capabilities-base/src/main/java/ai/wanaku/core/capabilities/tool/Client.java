package ai.wanaku.core.capabilities.tool;

import ai.wanaku.api.exceptions.WanakuException;
import ai.wanaku.core.config.provider.api.ConfigResource;
import ai.wanaku.core.exchange.ToolInvokeRequest;

/**
 * A client is responsible for exchanging data with a service
 */
public interface Client {
    /**
     * Exchange data with a service
     * @param request The tool invocation request as received by the MCP router
     * @param configResource the configuration resource to use for this tool invocation
     * @return Whatever response was obtained by calling the server (it must be convertible to a #java.lang.String)
     * @throws WanakuException if the operation cannot be executed
     */
    Object exchange(ToolInvokeRequest request, ConfigResource configResource) throws WanakuException;
}
