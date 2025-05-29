package ai.wanaku.server.quarkus.api.v1.management.discovery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ai.wanaku.api.types.WanakuResponse;
import ai.wanaku.api.types.management.State;
import ai.wanaku.core.mcp.providers.ServiceTarget;
import java.util.List;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/api/v1/management/discovery")
public class DiscoveryResource {
    private static final Logger LOG = Logger.getLogger(DiscoveryResource.class);

    @Inject
    DiscoveryBean discoveryBean;

    @Path("/tools/state")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public WanakuResponse<Map<String, List<State>>> toolsState() {
        return new WanakuResponse<>(discoveryBean.toolsState());
    }

    @Path("/resources/state")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    public WanakuResponse<Map<String, List<State>>> resourcesState() {
        return new WanakuResponse<>(discoveryBean.resourcesState());
    }

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(ServiceTarget serviceTarget) {
        discoveryBean.registerService(serviceTarget);
        return Response.ok().build();
    }

    @Path("/deregister")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deregister(ServiceTarget serviceTarget) {
        discoveryBean.deregisterService(serviceTarget);
        return Response.ok().build();
    }

    @Path("/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(ServiceTarget serviceTarget) {
        discoveryBean.registerService(serviceTarget);
        return Response.ok().build();
    }
}
