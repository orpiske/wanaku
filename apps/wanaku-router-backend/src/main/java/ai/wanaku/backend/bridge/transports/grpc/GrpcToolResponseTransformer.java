package ai.wanaku.backend.bridge.transports.grpc;

import java.util.ArrayList;
import java.util.List;
import ai.wanaku.backend.bridge.ToolResponseTransformer;
import ai.wanaku.backend.bridge.types.WanakuToolResult;
import ai.wanaku.core.exchange.v1.ToolInvokeReply;
import com.google.protobuf.ProtocolStringList;

class GrpcToolResponseTransformer implements ToolResponseTransformer<ToolInvokeReply> {

    @Override
    public WanakuToolResult transformReply(ToolInvokeReply reply) {
        ProtocolStringList contentList = reply.getContentList();
        List<String> contents = new ArrayList<>(contentList.size());
        contents.addAll(contentList);

        return WanakuToolResult.success(contents);
    }
}
