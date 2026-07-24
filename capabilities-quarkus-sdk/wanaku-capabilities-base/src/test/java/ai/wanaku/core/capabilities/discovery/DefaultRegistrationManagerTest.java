package ai.wanaku.core.capabilities.discovery;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import ai.wanaku.capabilities.sdk.api.types.WanakuResponse;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.core.capabilities.config.WanakuServiceConfig;
import ai.wanaku.core.service.discovery.client.DiscoveryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

class DefaultRegistrationManagerTest {

    private DiscoveryService discoveryService;
    private DefaultRegistrationManager manager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        discoveryService = mock(DiscoveryService.class);

        ServiceTarget target = ServiceTarget.newEmptyTarget("test-service", "localhost", 9090, "tool-invoker");
        target.setId("test-id-1");

        String serviceHome = tempDir.toString();
        WanakuServiceConfig config = createTestConfig("test-service", serviceHome);

        manager = new DefaultRegistrationManager(discoveryService, target, config);
    }

    @Test
    void registerShouldCallServiceWhenNotRegistered() {
        ServiceTarget responseTarget = newResponseTarget();
        doReturn(new WanakuResponse<>(responseTarget)).when(discoveryService).register(any());

        manager.register();

        // The service should have been called at least once for registration
        verify(discoveryService, atLeastOnce()).register(any());
    }

    @Test
    void registerCallsPingWhenAlreadyRegistered() {
        ServiceTarget responseTarget = newResponseTarget();

        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(invocation -> {
                    callCount.incrementAndGet();
                    return new WanakuResponse<>(responseTarget);
                })
                .when(discoveryService)
                .register(any());

        // First call: initial registration
        manager.register();
        int afterFirstRegister = callCount.get();
        assertTrue(afterFirstRegister >= 1, "Initial registration should call service at least once");

        // Second call: should go through ping() path
        manager.register();
        int afterPing = callCount.get();
        assertTrue(afterPing > afterFirstRegister, "Ping should call service to refresh registration");
    }

    @Test
    void pingFailureCausesReRegistration() {
        ServiceTarget responseTarget = newResponseTarget();

        // Initial registration succeeds
        doReturn(new WanakuResponse<>(responseTarget)).when(discoveryService).register(any());
        manager.register();

        // Simulate router down: ping fails
        reset(discoveryService);
        doThrow(new RuntimeException("Connection refused"))
                .when(discoveryService)
                .register(any());
        manager.register(); // ping fails, resets registered flag

        // Reset mock and count calls for re-registration
        reset(discoveryService);
        AtomicInteger reRegistrationCalls = new AtomicInteger(0);
        doAnswer(invocation -> {
                    reRegistrationCalls.incrementAndGet();
                    return new WanakuResponse<>(responseTarget);
                })
                .when(discoveryService)
                .register(any());

        // Next register should attempt full re-registration
        manager.register();
        assertTrue(
                reRegistrationCalls.get() >= 1, "After ping failure, register() should attempt full re-registration");
    }

    @Test
    void registerWhenNotRegisteredShouldAttemptRegistration() {
        // When not registered, register() should attempt full registration
        doReturn(new WanakuResponse<>(newResponseTarget()))
                .when(discoveryService)
                .register(any());
        manager.register();
        verify(discoveryService, atLeastOnce()).register(any());
    }

    @Test
    void multipleSuccessfulPingsDoNotTriggerReRegistration() {
        ServiceTarget responseTarget = newResponseTarget();

        AtomicInteger callCount = new AtomicInteger(0);
        doAnswer(invocation -> {
                    callCount.incrementAndGet();
                    return new WanakuResponse<>(responseTarget);
                })
                .when(discoveryService)
                .register(any());

        // Initial registration
        manager.register();
        int afterRegistration = callCount.get();

        // Multiple pings - each should make exactly 1 additional call (no retry logic)
        manager.register(); // ping 1
        int afterPing1 = callCount.get();
        assertEquals(afterRegistration + 1, afterPing1, "Each ping should make exactly 1 call");

        manager.register(); // ping 2
        int afterPing2 = callCount.get();
        assertEquals(afterPing1 + 1, afterPing2, "Each ping should make exactly 1 call");
    }

    private static ServiceTarget newResponseTarget() {
        ServiceTarget responseTarget = ServiceTarget.newEmptyTarget("test-service", "localhost", 9090, "tool-invoker");
        responseTarget.setId("test-id-1");
        return responseTarget;
    }

    private static WanakuServiceConfig createTestConfig(String name, String serviceHome) {
        return new WanakuServiceConfig() {
            @Override
            public String serviceHome() {
                return serviceHome;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String baseUri() {
                return "%s://%s";
            }

            @Override
            public Service service() {
                return new Service() {
                    @Override
                    public Map<String, String> defaults() {
                        return Map.of();
                    }

                    @Override
                    public Set<Property> properties() {
                        return Set.of();
                    }
                };
            }

            @Override
            public Registration registration() {
                return new Registration() {
                    @Override
                    public String interval() {
                        return "10s";
                    }

                    @Override
                    public int delaySeconds() {
                        return 0;
                    }

                    @Override
                    public int retries() {
                        return 3;
                    }

                    @Override
                    public int retryWaitSeconds() {
                        return 0;
                    }

                    @Override
                    public String uri() {
                        return "http://localhost:8080";
                    }

                    @Override
                    public String announceAddress() {
                        return "auto";
                    }
                };
            }
        };
    }
}
