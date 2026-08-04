package ai.wanaku.backend.bridge.types;

import java.util.Map;

public class WanakuToolCallContext {

    private final Map<String, Object> args;
    private final String connectionId;
    private final String requestId;

    public WanakuToolCallContext(Map<String, Object> args, String connectionId, String requestId) {
        this.args = args;
        this.connectionId = connectionId;
        this.requestId = requestId;
    }

    public Map<String, Object> args() {
        return args;
    }

    public String connectionId() {
        return connectionId;
    }

    public String requestId() {
        return requestId;
    }
}
