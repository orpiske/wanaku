package ai.wanaku.backend.proxies;

import ai.wanaku.capabilities.sdk.api.types.io.ToolPayload;

/**
 * Proxies between MCP URIs and Camel components capable of handling them.
 * This interface is now decoupled from the Tool interface to maintain separation of concerns.
 */
public interface ToolsProxy extends Proxy, Provisionable<ToolPayload> {}
