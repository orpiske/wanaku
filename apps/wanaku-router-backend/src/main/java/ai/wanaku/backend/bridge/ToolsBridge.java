package ai.wanaku.backend.bridge;

import io.smallrye.mutiny.Uni;
import ai.wanaku.backend.bridge.types.WanakuToolCallContext;
import ai.wanaku.backend.bridge.types.WanakuToolResult;
import ai.wanaku.capabilities.sdk.api.types.CallableReference;

public interface ToolsBridge extends Bridge {

    Uni<WanakuToolResult> execute(WanakuToolCallContext toolCallContext, CallableReference toolReference);
}
