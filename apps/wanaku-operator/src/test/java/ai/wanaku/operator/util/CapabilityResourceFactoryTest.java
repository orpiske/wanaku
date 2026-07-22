package ai.wanaku.operator.util;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import ai.wanaku.operator.wanaku.WanakuCapability;
import ai.wanaku.operator.wanaku.WanakuCapabilitySpec;
import ai.wanaku.operator.wanaku.WanakuTypes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CapabilityResourceFactoryTest {

    @Test
    void cicDeploymentHasStartupProbe() {
        WanakuCapability capability = createCapability();
        WanakuCapabilitySpec.CapabilitiesSpec capSpec = createCicCapabilitiesSpec();

        Deployment deployment = CapabilityResourceFactory.makeDesiredCiCCapabilityDeployment(capability, null, capSpec);

        Container container =
                deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Probe startupProbe = container.getStartupProbe();

        assertNotNull(startupProbe, "CIC deployment must have a startup probe");
        assertNotNull(startupProbe.getTcpSocket(), "Startup probe must use TCP socket check");
        assertEquals(9190, startupProbe.getTcpSocket().getPort().getIntVal(), "Startup probe must check port 9190");
        assertNotNull(startupProbe.getFailureThreshold(), "Startup probe must define a failure threshold");
    }

    @Test
    void wanakuCapabilityDeploymentHasStartupProbe() {
        WanakuCapability capability = createCapability();
        WanakuCapabilitySpec.CapabilitiesSpec capSpec = createWanakuCapabilitiesSpec();

        Deployment deployment =
                CapabilityResourceFactory.makeDesiredWanakuCapabilityDeployment(capability, null, capSpec);

        Container container =
                deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
        Probe startupProbe = container.getStartupProbe();

        assertNotNull(startupProbe, "Wanaku capability deployment must have a startup probe");
        assertNotNull(startupProbe.getTcpSocket(), "Startup probe must use TCP socket check");
        assertEquals(9000, startupProbe.getTcpSocket().getPort().getIntVal(), "Startup probe must check port 9000");
        assertNotNull(startupProbe.getFailureThreshold(), "Startup probe must define a failure threshold");
    }

    @Test
    void cicDeploymentRetainsLivenessAndReadinessProbes() {
        WanakuCapability capability = createCapability();
        WanakuCapabilitySpec.CapabilitiesSpec capSpec = createCicCapabilitiesSpec();

        Deployment deployment = CapabilityResourceFactory.makeDesiredCiCCapabilityDeployment(capability, null, capSpec);

        Container container =
                deployment.getSpec().getTemplate().getSpec().getContainers().get(0);

        assertNotNull(container.getLivenessProbe(), "CIC deployment must have a liveness probe");
        assertNotNull(container.getReadinessProbe(), "CIC deployment must have a readiness probe");
        assertNotNull(container.getLivenessProbe().getTcpSocket(), "Liveness probe must use TCP socket check");
        assertNotNull(container.getReadinessProbe().getTcpSocket(), "Readiness probe must use TCP socket check");
    }

    private static WanakuCapability createCapability() {
        WanakuCapability capability = new WanakuCapability();
        capability.setMetadata(new ObjectMetaBuilder()
                .withName("test-capability")
                .withNamespace("default")
                .withUid("test-uid-cap")
                .build());
        WanakuCapabilitySpec spec = new WanakuCapabilitySpec();
        spec.setRouterRef("test-router");

        WanakuTypes.AuthSpec auth = new WanakuTypes.AuthSpec();
        auth.setAuthServer("http://keycloak:8080");
        spec.setAuth(auth);

        WanakuTypes.SecretsSpec secrets = new WanakuTypes.SecretsSpec();
        secrets.setOidcCredentialsSecret("test-secret");
        spec.setSecrets(secrets);

        capability.setSpec(spec);
        return capability;
    }

    private static WanakuCapabilitySpec.CapabilitiesSpec createCicCapabilitiesSpec() {
        WanakuCapabilitySpec.CapabilitiesSpec capSpec = new WanakuCapabilitySpec.CapabilitiesSpec();
        capSpec.setName("test-cic");
        capSpec.setImage("quay.io/wanaku/camel-integration-capability:latest");
        capSpec.setType("camel-integration-capability");
        capSpec.setServiceCatalog("hello-system");
        capSpec.setServiceCatalogSystem("hello-system");
        return capSpec;
    }

    private static WanakuCapabilitySpec.CapabilitiesSpec createWanakuCapabilitiesSpec() {
        WanakuCapabilitySpec.CapabilitiesSpec capSpec = new WanakuCapabilitySpec.CapabilitiesSpec();
        capSpec.setName("test-capability");
        capSpec.setImage("quay.io/wanaku/wanaku-capability:latest");
        return capSpec;
    }
}
