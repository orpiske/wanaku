package ai.wanaku.core.persistence.infinispan;

import jakarta.inject.Inject;

import ai.wanaku.api.types.management.Service;
import ai.wanaku.api.types.management.State;
import ai.wanaku.core.mcp.providers.ServiceRegistry;
import ai.wanaku.core.mcp.providers.ServiceTarget;
import ai.wanaku.core.mcp.providers.ServiceType;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceRegistryTest {

    @Inject
    ServiceRegistry serviceRegistry;

    private static final String TEST_SERVICE_NAME = "myService";

    @BeforeAll
    public void setup() {
        ((InfinispanServiceRegistry) serviceRegistry).clear();
    }

    @Test
    @Order(1)
    public void register() {
        ServiceTarget serviceTarget = new ServiceTarget(TEST_SERVICE_NAME, "localhost", 8081, ServiceType.TOOL_INVOKER, Map.of("myProperty", "myDescription"));

        Assertions.assertDoesNotThrow(() -> serviceRegistry.register(serviceTarget));
    }

    @Test
    @Order(2)
    public void getService() {
        Service service = serviceRegistry.getService(TEST_SERVICE_NAME, ServiceType.TOOL_INVOKER);

        Assertions.assertEquals("localhost:8081", service.getTarget());
    }

    @Test
    @Order(3)
    public void getServiceByType() {
        List<ServiceTarget> tools = serviceRegistry.getEntries(ServiceType.TOOL_INVOKER);
        List<ServiceTarget> resources = serviceRegistry.getEntries(ServiceType.RESOURCE_PROVIDER);

        Assertions.assertEquals(1, tools.size());
        Assertions.assertEquals(0, resources.size());
        Assertions.assertEquals(TEST_SERVICE_NAME, tools.getFirst().getService());
    }

    @Test
    @Order(4)
    public void saveState() {
        serviceRegistry.saveState(TEST_SERVICE_NAME, true, "myMessage");

        // TODO
//        Assertions.assertTrue(Files.readString(statusPath).contains("myMessage"));
    }

    @Test
    @Order(5)
    public void getState() {
        List<State> states = serviceRegistry.getState(TEST_SERVICE_NAME, 10);

        // TODO
//        Assertions.assertEquals(1, states.size());
    }

    @Test
    @Order(6)
    public void updateProperty() {
        ServiceTarget serviceTarget = new ServiceTarget(TEST_SERVICE_NAME, "localhost", 8081, ServiceType.TOOL_INVOKER, Map.of("myProperty", "myDescription-2"));

        serviceRegistry.update(serviceTarget);

        List<ServiceTarget> tools = serviceRegistry.getEntries(ServiceType.TOOL_INVOKER);

        Assertions.assertEquals(1, tools.size());
        Assertions.assertEquals(TEST_SERVICE_NAME, tools.getFirst().getService());

        final ServiceTarget service = tools.getFirst();

        Assertions.assertEquals("myDescription-2", service.getConfigurations().get("myProperty"));
    }

    @Test
    @Order(7)
    public void deregister() {
        ServiceTarget serviceTarget = new ServiceTarget(TEST_SERVICE_NAME, "localhost", 0, ServiceType.TOOL_INVOKER, Map.of());

        // The testServiceName is a TOOL, this line of code should not deregister
        serviceRegistry.deregister(serviceTarget);

        List<ServiceTarget> tools = serviceRegistry.getEntries(ServiceType.TOOL_INVOKER);
        Assertions.assertEquals(0, tools.size());
    }
}
