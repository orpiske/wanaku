package ai.wanaku.core.service.discovery.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ai.wanaku.api.types.providers.ServiceTarget;

@Path("/api/v1/management/discovery")
public interface DiscoveryService {

    @Path("/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response register(ServiceTarget serviceTarget);

    @Path("/deregister")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response deregister(ServiceTarget serviceTarget);

    @Path("/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(ServiceTarget serviceTarget);
}
