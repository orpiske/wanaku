package ai.wanaku.backend.bridge.types;

import java.util.List;

public class WanakuResourceResult {

    private final List<WanakuResourceContent> contents;

    public WanakuResourceResult(List<WanakuResourceContent> contents) {
        this.contents = contents;
    }

    public List<WanakuResourceContent> contents() {
        return contents;
    }
}
