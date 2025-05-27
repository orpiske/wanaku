package ai.wanaku.core.persistence.infinispan.marshaller;

import ai.wanaku.core.persistence.types.ForwardEntity;
import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class ForwardMarshaller implements MessageMarshaller<ForwardEntity> {
    @Override
    public ForwardEntity readFrom(ProtoStreamReader reader) throws IOException {
        String name = reader.readString("name");
        String address = reader.readString("address");

        final ForwardEntity forwardEntity = new ForwardEntity();
        forwardEntity.setId(name);
        forwardEntity.setName(name);
        forwardEntity.setAddress(address);

        return forwardEntity;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, ForwardEntity forwardEntity) throws IOException {
        writer.writeString("name", forwardEntity.getName());
        writer.writeString("address", forwardEntity.getAddress());
    }

    @Override
    public Class<? extends ForwardEntity> getJavaClass() {
        return ForwardEntity.class;
    }

    @Override
    public String getTypeName() {
        return "wanaku.ForwardReference";
    }
}
