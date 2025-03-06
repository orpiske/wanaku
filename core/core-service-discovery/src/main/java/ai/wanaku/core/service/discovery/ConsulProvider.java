package ai.wanaku.core.service.discovery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.vertx.core.Vertx;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConsulProvider {

    @ConfigProperty(name = "consul.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "consul.port", defaultValue = "8500")
    int port;

    @Produces
    public ConsulClient consulClient(Vertx vertx) {
        return ConsulClient.create(vertx, new ConsulClientOptions().setHost(host).setPort(port));
    }
}
