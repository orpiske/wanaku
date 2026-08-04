package ai.wanaku.backend.bridge;

import io.smallrye.mutiny.Uni;
import ai.wanaku.backend.bridge.types.WanakuResourceReadContext;
import ai.wanaku.backend.bridge.types.WanakuResourceResult;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;

public interface ResourceBridge extends Bridge {

    Uni<WanakuResourceResult> read(WanakuResourceReadContext readContext, ResourceReference mcpResource);
}
