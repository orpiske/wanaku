package ai.wanaku.backend.bridge;

import ai.wanaku.backend.bridge.types.WanakuToolResult;

public interface ToolResponseTransformer<T> {

    WanakuToolResult transformReply(T reply);
}
