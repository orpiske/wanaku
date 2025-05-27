package ai.wanaku.core.persistence.types;

import ai.wanaku.api.types.ToolReference;
//import org.infinispan.protostream.annotations.Proto;

//@Proto
public class ToolReferenceEntity extends ToolReference implements WanakuEntity {
    @Override
    public String getId() {
        return getName();
    }

    @Override
    public void setId(String id) {
        setName(id);
    }
}
