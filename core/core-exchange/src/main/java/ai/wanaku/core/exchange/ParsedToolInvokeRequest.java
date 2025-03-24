package ai.wanaku.core.exchange;


import ai.wanaku.core.uri.Parameter;
import ai.wanaku.core.uri.URIParser;
import java.util.Collections;
import java.util.Map;

/**
 * Represents a parsed tool invocation request containing the URI and its body.
 */
public record ParsedToolInvokeRequest(String uri, String body) {

    /**
     * Parses the URI provided by the router
     * @param toolInvokeRequest the invocation request (containing the arguments and the request URI)
     * @return the parsed URI and its body
     */
    public static ParsedToolInvokeRequest parseRequest(ToolInvokeRequest toolInvokeRequest) {
        String uri = toolInvokeRequest.getUri();
        return parseRequest(uri, toolInvokeRequest);
    }

    /**
     * Parses a URI, ignoring the one provided by the router. This is used for services that rely on simplified
     * tools/resources URI to hide the complexity of the implementation details (i.e.: such as overly complex Camel URIs).
     * @param uri the actual URI that will be parsed. The parameters will be merged w/ the provided URI from the router.
     * @param toolInvokeRequest the invocation request (containing the arguments and the request URI)
     * @return the parsed URI and its body
     */
    public static ParsedToolInvokeRequest parseRequest(String uri, ToolInvokeRequest toolInvokeRequest) {
        Map<String, String> argumentsMap = toolInvokeRequest.getArgumentsMap();

        if (argumentsMap == null) {
            argumentsMap = Collections.emptyMap();
        }

        String parsedUri = URIParser.parse(uri, Map.of(Parameter.KEY_NAME, new Parameter((Map) argumentsMap)));

        String body = toolInvokeRequest.getBody();

        return new ParsedToolInvokeRequest(parsedUri, body);
    }
}
