package ai.wanaku.backend.bridge.types;

public class WanakuResourceReadContext {

    private final String requestId;
    private final String connectionId;
    private final String requestUri;

    public WanakuResourceReadContext(String requestId, String connectionId, String requestUri) {
        this.requestId = requestId;
        this.connectionId = connectionId;
        this.requestUri = requestUri;
    }

    public String requestId() {
        return requestId;
    }

    public String connectionId() {
        return connectionId;
    }

    public String requestUri() {
        return requestUri;
    }
}
