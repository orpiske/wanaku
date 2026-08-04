package ai.wanaku.backend.bridge.transports.grpc;

import java.util.ArrayList;
import java.util.List;
import ai.wanaku.backend.bridge.ResourceResponseTransformer;
import ai.wanaku.backend.bridge.types.WanakuResourceContent;
import ai.wanaku.backend.bridge.types.WanakuResourceReadContext;
import ai.wanaku.capabilities.sdk.api.types.ResourceReference;
import ai.wanaku.core.exchange.v1.ResourceReply;
import com.google.protobuf.ProtocolStringList;

class GrpcResourceResponseTransformer implements ResourceResponseTransformer<ResourceReply> {

    @Override
    public List<WanakuResourceContent> transformReply(
            ResourceReply reply, WanakuResourceReadContext readContext, ResourceReference mcpResource) {
        ProtocolStringList contentList = reply.getContentList();
        List<WanakuResourceContent> resourceContentsList = new ArrayList<>();
        for (String content : contentList) {
            WanakuResourceContent resourceContent =
                    new WanakuResourceContent(readContext.requestUri(), content, mcpResource.getMimeType());
            resourceContentsList.add(resourceContent);
        }
        return resourceContentsList;
    }
}
