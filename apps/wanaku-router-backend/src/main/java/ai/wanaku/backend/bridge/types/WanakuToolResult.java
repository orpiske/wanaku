package ai.wanaku.backend.bridge.types;

import java.util.List;

public class WanakuToolResult {

    private final List<String> contents;
    private final String errorMessage;
    private final boolean error;

    private WanakuToolResult(List<String> contents, String errorMessage, boolean error) {
        this.contents = contents;
        this.errorMessage = errorMessage;
        this.error = error;
    }

    public static WanakuToolResult success(List<String> contents) {
        return new WanakuToolResult(contents, null, false);
    }

    public static WanakuToolResult error(String message) {
        return new WanakuToolResult(List.of(), message, true);
    }

    public List<String> contents() {
        return contents;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        if (error) {
            return "Error: " + errorMessage;
        }
        return String.join(", ", contents);
    }
}
