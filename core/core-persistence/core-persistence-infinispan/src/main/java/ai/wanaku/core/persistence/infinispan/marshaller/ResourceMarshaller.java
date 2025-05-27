package ai.wanaku.core.persistence.infinispan.marshaller;

import ai.wanaku.api.types.ResourceReference;
import ai.wanaku.core.persistence.types.ResourceReferenceEntity;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.infinispan.protostream.MessageMarshaller;

public class ResourceMarshaller implements MessageMarshaller<ResourceReferenceEntity> {
    @Override
    public ResourceReferenceEntity readFrom(ProtoStreamReader reader) throws IOException {
        String name = reader.readString("name");
        String type = reader.readString("type");
        String location = reader.readString("location");
        String description = reader.readString("description");
        String mimeType = reader.readString("mimeType");

//        Map<String, String> params = new HashMap<>();
//        reader.readMap("params", params);

        final ResourceReferenceEntity resourceReference = new ResourceReferenceEntity();
        resourceReference.setName(name);
        resourceReference.setType(type);
        resourceReference.setLocation(location);
        resourceReference.setDescription(description);
        resourceReference.setMimeType(mimeType);
//        resourceReference.setParams(params);

        return resourceReference;
    }

    @Override
    public void writeTo(ProtoStreamWriter writer, ResourceReferenceEntity resourceReferenceEntity) throws IOException {
        writer.writeString("name", resourceReferenceEntity.getName());
        writer.writeString("type", resourceReferenceEntity.getType());
        writer.writeString("location", resourceReferenceEntity.getLocation());
        writer.writeString("description", resourceReferenceEntity.getDescription());
        writer.writeString("mimeType", resourceReferenceEntity.getMimeType());

        // Serialize the map
        List<ResourceReference.Param> params = resourceReferenceEntity.getParams();
        writer.writeCollection("params", params, ResourceReference.Param.class);
    }

    @Override
    public Class<? extends ResourceReferenceEntity> getJavaClass() {
        return ResourceReferenceEntity.class;
    }

    @Override
    public String getTypeName() {
        return "wanaku.ResourceReference";
    }
}
