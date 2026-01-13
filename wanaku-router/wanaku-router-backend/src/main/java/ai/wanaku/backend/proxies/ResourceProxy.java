package ai.wanaku.backend.proxies;

import ai.wanaku.capabilities.sdk.api.types.ResourceReference;
import ai.wanaku.capabilities.sdk.api.types.io.ResourcePayload;
import io.quarkiverse.mcp.server.ResourceContents;
import io.quarkiverse.mcp.server.ResourceManager;
import java.util.List;

/**
 * Proxies between MCP URIs and Camel components capable of handling them
 */
public interface ResourceProxy extends Proxy, Provisionable<ResourcePayload> {

    /**
     * Eval an MCP URI handling it as appropriate by the component (i.e.: read a file, GET a static web page, etc.)
     * @param arguments the resource request arguments
     * @param mcpResource the resource to eval
     * @return Returns the data read by the proxy.
     */
    List<ResourceContents> eval(ResourceManager.ResourceArguments arguments, ResourceReference mcpResource);
}
