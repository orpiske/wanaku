package ai.wanaku.api.types.discovery;

import ai.wanaku.api.types.WanakuEntity;
import java.time.Instant;

public class ServiceRecord implements WanakuEntity {
    private String id;
    private Instant lastSeen;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
