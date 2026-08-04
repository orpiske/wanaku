package ai.wanaku.backend.bridge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ai.wanaku.backend.bridge.types.WanakuToolCallContext;
import ai.wanaku.backend.bridge.types.WanakuToolResult;
import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.capabilities.sdk.api.types.InputSchema;
import ai.wanaku.capabilities.sdk.api.types.Property;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InvokerBridgeTest {

    private ToolReference buildToolReference(Map<String, Property> properties) {
        InputSchema schema = new InputSchema();
        schema.setType("object");
        schema.setProperties(properties);

        ToolReference ref = new ToolReference();
        ref.setName("sample");
        ref.setType("http");
        ref.setUri("https://example-app/hello-world/v1/test");
        ref.setInputSchema(schema);
        return ref;
    }

    private static Property prop(String target, String scope, String value) {
        Property p = new Property();
        p.setType("string");
        p.setDescription("test");
        p.setTarget(target);
        p.setScope(scope);
        p.setValue(value);
        return p;
    }

    private static WanakuToolCallContext emptyToolCallContext() {
        return new WanakuToolCallContext(Map.of(), "test-connection", "test-request");
    }

    private static WanakuToolCallContext toolCallContextWithArgs(Map<String, Object> args) {
        return new WanakuToolCallContext(args, "test-connection", "test-request");
    }

    @Test
    void extractHeaders_onlyHeaderAndServiceScopeAreReturned() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-Request-ID", prop("header", "service", "abc-123"));
        props.put("CamelHttpMethod", prop("header", "service", "GET"));
        props.put("name", prop(null, "service", null));

        ToolReference ref = buildToolReference(props);

        WanakuToolCallContext ctx = emptyToolCallContext();
        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);

        assertEquals(2, headers.size(), "Should only include header+service entries");
        assertEquals("abc-123", headers.get("X-Request-ID"));
        assertEquals("GET", headers.get("CamelHttpMethod"));
        assertFalse(headers.containsKey("name"), "Non-header properties must be ignored");
    }

    @Test
    void extractHeaders_ignoresNonServiceScope() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-Request-ID", prop("header", "service", "xyz"));
        props.put("Some-Other-Header", prop("header", "service-endpoint", "should-be-ignored"));

        ToolReference ref = buildToolReference(props);

        WanakuToolCallContext ctx = emptyToolCallContext();
        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);

        assertEquals(1, headers.size());
        assertEquals("xyz", headers.get("X-Request-ID"));
        assertFalse(headers.containsKey("Some-Other-Header"));
    }

    @Test
    void extractHeaders_returnsEmptyMapWhenNoHeaders() {
        Map<String, Property> props = new HashMap<>();
        props.put("name", prop(null, "service", null));
        props.put("id", prop(null, "service", null));

        ToolReference ref = buildToolReference(props);

        WanakuToolCallContext ctx = emptyToolCallContext();
        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);
        assertTrue(headers.isEmpty());
    }

    @Test
    void extractHeaders_throwsNPEWhenPropertyHasNoDefaultValueAndArgumentNotProvided() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-API-Key", prop("header", "service", null));

        ToolReference ref = buildToolReference(props);
        WanakuToolCallContext ctx = emptyToolCallContext();

        assertThrows(
                NullPointerException.class,
                () -> InvokerToolExecutor.extractHeaders(ref, ctx),
                "Should throw NPE when property value is null and argument is not provided");
    }

    @Test
    void extractHeaders_usesTheArgumentValueFromToolInvocation() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-API-Key", prop("header", "service", null));

        ToolReference ref = buildToolReference(props);
        WanakuToolCallContext ctx = toolCallContextWithArgs(Map.of("X-API-Key", "123"));

        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);
        assertEquals("123", headers.get("X-API-Key"));
    }

    @Test
    void extractHeaders_usesTheDefaultArgument() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-API-Key", prop("header", "service", "abc"));

        ToolReference ref = buildToolReference(props);
        WanakuToolCallContext ctx = emptyToolCallContext();

        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);
        assertEquals("abc", headers.get("X-API-Key"));
    }

    @Test
    void extractHeaders_prefersTheProvidedArgument() {
        Map<String, Property> props = new HashMap<>();
        props.put("X-API-Key", prop("header", "service", "abc"));

        ToolReference ref = buildToolReference(props);
        WanakuToolCallContext ctx = toolCallContextWithArgs(Map.of("X-API-Key", "123"));

        Map<String, String> headers = InvokerToolExecutor.extractHeaders(ref, ctx);
        assertEquals("123", headers.get("X-API-Key"));
    }

    @Test
    void extractMetadataHeaders_extractsPrefixedArgsAndStripsPrefix() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_meta_contextId", "ctx-123");
        args.put("wanaku_meta_userId", "user-456");
        args.put("regularArg", "value");
        WanakuToolCallContext ctx = toolCallContextWithArgs(args);

        Map<String, String> headers = InvokerToolExecutor.extractMetadataHeaders(ctx);

        assertEquals(2, headers.size());
        assertEquals("ctx-123", headers.get("contextId"));
        assertEquals("user-456", headers.get("userId"));
        assertFalse(headers.containsKey("regularArg"));
    }

    @Test
    void extractMetadataHeaders_handlesNullValues() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_meta_contextId", "ctx-123");
        args.put("wanaku_meta_nullValue", null);
        WanakuToolCallContext ctx = toolCallContextWithArgs(args);

        Map<String, String> headers = InvokerToolExecutor.extractMetadataHeaders(ctx);

        assertEquals(1, headers.size());
        assertEquals("ctx-123", headers.get("contextId"));
    }

    @Test
    void extractMetadataHeaders_returnsEmptyMapWhenNoMetadataArgs() {
        WanakuToolCallContext ctx = toolCallContextWithArgs(Map.of("regularArg", "value"));

        Map<String, String> headers = InvokerToolExecutor.extractMetadataHeaders(ctx);
        assertTrue(headers.isEmpty());
    }

    @Test
    void filterOutReservedArgs_removesMetadataPrefixedArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_meta_contextId", "ctx-123");
        args.put("wanaku_meta_userId", "user-456");
        args.put("regularArg", "value");
        args.put("anotherArg", 42);

        Map<String, Object> filtered = InvokerToolExecutor.filterOutReservedArgs(args);

        assertEquals(2, filtered.size());
        assertEquals("value", filtered.get("regularArg"));
        assertEquals(42, filtered.get("anotherArg"));
    }

    @Test
    void filterOutReservedArgs_removesAuthPrefixedArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_auth_Authorization", "Bearer token-123");
        args.put("wanaku_auth_X-Third-Party", "secret-456");
        args.put("regularArg", "value");

        Map<String, Object> filtered = InvokerToolExecutor.filterOutReservedArgs(args);

        assertEquals(1, filtered.size());
        assertEquals("value", filtered.get("regularArg"));
    }

    @Test
    void filterOutReservedArgs_removesBothMetadataAndAuthArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_meta_contextId", "ctx-123");
        args.put("wanaku_auth_Authorization", "Bearer token-123");
        args.put("regularArg", "value");

        Map<String, Object> filtered = InvokerToolExecutor.filterOutReservedArgs(args);

        assertEquals(1, filtered.size());
        assertEquals("value", filtered.get("regularArg"));
    }

    @Test
    void filterOutReservedArgs_returnsAllArgsWhenNoReserved() {
        Map<String, Object> args = new HashMap<>();
        args.put("regularArg", "value");
        args.put("anotherArg", 42);

        Map<String, Object> filtered = InvokerToolExecutor.filterOutReservedArgs(args);

        assertEquals(2, filtered.size());
    }

    @Test
    void extractAuthHeaders_extractsPrefixedArgsAndStripsPrefix() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_auth_Authorization", "Bearer token-123");
        args.put("wanaku_auth_X-Third-Party", "secret-456");
        args.put("regularArg", "value");
        WanakuToolCallContext ctx = toolCallContextWithArgs(args);

        Map<String, String> headers = InvokerToolExecutor.extractAuthHeaders(ctx);

        assertEquals(2, headers.size());
        assertEquals("Bearer token-123", headers.get("Authorization"));
        assertEquals("secret-456", headers.get("X-Third-Party"));
    }

    @Test
    void extractAuthHeaders_handlesNullValues() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_auth_Authorization", "Bearer token-123");
        args.put("wanaku_auth_nullValue", null);
        WanakuToolCallContext ctx = toolCallContextWithArgs(args);

        Map<String, String> headers = InvokerToolExecutor.extractAuthHeaders(ctx);

        assertEquals(1, headers.size());
        assertEquals("Bearer token-123", headers.get("Authorization"));
    }

    @Test
    void extractAuthHeaders_returnsEmptyMapWhenNoAuthArgs() {
        WanakuToolCallContext ctx = toolCallContextWithArgs(Map.of("regularArg", "value"));

        Map<String, String> headers = InvokerToolExecutor.extractAuthHeaders(ctx);
        assertTrue(headers.isEmpty());
    }

    @Test
    void extractAuthHeaders_doesNotInterfereWithMetadataHeaders() {
        Map<String, Object> args = new HashMap<>();
        args.put("wanaku_meta_contextId", "ctx-123");
        args.put("wanaku_auth_Authorization", "Bearer token-123");
        args.put("regularArg", "value");
        WanakuToolCallContext ctx = toolCallContextWithArgs(args);

        Map<String, String> authHeaders = InvokerToolExecutor.extractAuthHeaders(ctx);
        Map<String, String> metaHeaders = InvokerToolExecutor.extractMetadataHeaders(ctx);

        assertEquals(1, authHeaders.size());
        assertEquals("Bearer token-123", authHeaders.get("Authorization"));

        assertEquals(1, metaHeaders.size());
        assertEquals("ctx-123", metaHeaders.get("contextId"));
    }

    @Test
    void validateRequiredParameters_throwsWhenRequiredParamMissing() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query"));

        Map<String, Object> args = Map.of();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> InvokerToolExecutor.validateRequiredParameters(ref, args));
        assertTrue(ex.getMessage().contains("query"));
    }

    @Test
    void validateRequiredParameters_throwsWhenRequiredParamIsBlank() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query"));

        Map<String, Object> args = Map.of("query", "   ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> InvokerToolExecutor.validateRequiredParameters(ref, args));
        assertTrue(ex.getMessage().contains("query"));
    }

    @Test
    void validateRequiredParameters_throwsWhenRequiredParamIsNull() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query"));

        Map<String, Object> args = new HashMap<>();
        args.put("query", null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> InvokerToolExecutor.validateRequiredParameters(ref, args));
        assertTrue(ex.getMessage().contains("query"));
    }

    @Test
    void validateRequiredParameters_passesWhenAllRequiredPresent() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query"));

        Map<String, Object> args = Map.of("query", "search term");

        assertDoesNotThrow(() -> InvokerToolExecutor.validateRequiredParameters(ref, args));
    }

    @Test
    void validateRequiredParameters_passesWhenNoRequiredList() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);

        Map<String, Object> args = Map.of();

        assertDoesNotThrow(() -> InvokerToolExecutor.validateRequiredParameters(ref, args));
    }

    @Test
    void validateRequiredParameters_passesWhenRequiredListIsEmpty() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of());

        Map<String, Object> args = Map.of();

        assertDoesNotThrow(() -> InvokerToolExecutor.validateRequiredParameters(ref, args));
    }

    @Test
    void validateRequiredParameters_passesWhenInputSchemaIsNull() {
        ToolReference ref = new ToolReference();
        ref.setName("sample");
        ref.setType("http");
        ref.setUri("https://example.com");

        Map<String, Object> args = Map.of();

        assertDoesNotThrow(() -> InvokerToolExecutor.validateRequiredParameters(ref, args));
    }

    @Test
    void validateRequiredParameters_reportsAllMissingParams() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));
        props.put("format", prop(null, null, null));
        props.put("limit", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query", "format", "limit"));

        Map<String, Object> args = Map.of("format", "json");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> InvokerToolExecutor.validateRequiredParameters(ref, args));
        assertTrue(ex.getMessage().contains("query"));
        assertTrue(ex.getMessage().contains("limit"));
        assertFalse(ex.getMessage().contains("format"));
    }

    @Test
    void validateRequiredParameters_throwsWhenArgsIsNull() {
        Map<String, Property> props = new HashMap<>();
        props.put("query", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.getInputSchema().setRequired(List.of("query"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> InvokerToolExecutor.validateRequiredParameters(ref, null));
        assertTrue(ex.getMessage().contains("query"));
    }

    @Test
    void execute_returnsErrorResponseOnValidationFailure() {
        Map<String, Property> props = new HashMap<>();
        props.put("city", prop(null, null, null));

        ToolReference ref = buildToolReference(props);
        ref.setType("http");
        ref.getInputSchema().setRequired(List.of("city"));

        WanakuToolCallContext ctx = emptyToolCallContext();

        ServiceResolver serviceResolver = mock(ServiceResolver.class);
        when(serviceResolver.resolve(anyString(), anyString()))
                .thenReturn(mock(ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget.class));

        WanakuBridgeTransport transport = mock(WanakuBridgeTransport.class);

        InvokerBridge bridge = new InvokerBridge(serviceResolver, transport, null, null);

        WanakuToolResult response = bridge.execute(ctx, ref).await().indefinitely();

        assertNotNull(response, "Response should not be null");
        assertTrue(response.isError(), "Response should be an error");
        assertTrue(
                response.errorMessage().contains("city"), "Error message should mention the missing parameter 'city'");
    }
}
