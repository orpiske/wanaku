package ai.wanaku.backend.bridge.types;

public class WanakuResourceContent {

    private final String uri;
    private final String text;
    private final String mimeType;

    public WanakuResourceContent(String uri, String text, String mimeType) {
        this.uri = uri;
        this.text = text;
        this.mimeType = mimeType;
    }

    public String uri() {
        return uri;
    }

    public String text() {
        return text;
    }

    public String mimeType() {
        return mimeType;
    }
}
