package ai.wanaku.backend.bridge;

import java.util.List;
import ai.wanaku.backend.bridge.types.WanakuResourceContent;
import ai.wanaku.backend.bridge.types.WanakuResourceReadContext;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;

public interface ResourceResponseTransformer<T> {

    List<WanakuResourceContent> transformReply(
            T reply, WanakuResourceReadContext readContext, ResourceReference mcpResource);
}
