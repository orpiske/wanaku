package ai.wanaku.core.service.discovery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ai.wanaku.api.types.management.Service;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ServiceList;
import io.vertx.ext.consul.ServiceOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ServiceRegistry {
    private static final Logger LOG = Logger.getLogger(ServiceRegistry.class);

    @Inject
    ConsulClient consulClient;

    public void register(String service, String address, final int port) {
        consulClient.registerService(new ServiceOptions()
                        .setPort(port)
                        .setAddress(address)
                        .setName(service)
                        .setId(toId(address, port)),
                result -> LOG.infof("Service %s %s-%d registered", service, address, port));

    }


    public void deregister(String service, String address, final int port) {
        consulClient.deregisterService(toId(address, port),
                result -> LOG.infof("Service %s %s-%d deregistered", service, address, port));
    }

    private static String toId(String address, int port) {
        return address + "-" + port;
    }

    public Map<String, Service> getEntries() {
        ServiceList result = consulClient.catalogServices().result();
        List<io.vertx.ext.consul.Service> list = result.getList();
        Map<String, Service> map = new HashMap<>();
        for (var service : list) {
            Service wanakuService = new Service();

            wanakuService.setTarget(service.getAddress());
            map.put(service.getName(), wanakuService);
        }

        return map;
    }
}
