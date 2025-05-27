package ai.wanaku.core.persistence.infinispan.marshaller;

import ai.wanaku.core.persistence.types.ToolReferenceEntity;
import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class ToolReferenceEntityMarshaller implements MessageMarshaller<ToolReferenceEntity> {
    @Override
    public ToolReferenceEntity readFrom(ProtoStreamReader reader) throws IOException {
        String name = reader.readString("name");
        String uri = reader.readString("uri");
        String type = reader.readString("type");
        String description = reader.readString("description");

        final ToolReferenceEntity toolReference = new ToolReferenceEntity();
        toolReference.setName(name);
        toolReference.setUri(uri);
        toolReference.setType(type);
        toolReference.setDescription(description);

        return toolReference;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, ToolReferenceEntity toolReference) throws IOException {
        writer.writeString("name", toolReference.getName());
        writer.writeString("uri", toolReference.getUri());
        writer.writeString("type", toolReference.getType());
        writer.writeString("description", toolReference.getDescription());
    }

    @Override
    public Class<? extends ToolReferenceEntity> getJavaClass() {
        return ToolReferenceEntity.class;
    }

    @Override
    public String getTypeName() {
        return "wanaku.ToolReferenceEntity";
    }
}
