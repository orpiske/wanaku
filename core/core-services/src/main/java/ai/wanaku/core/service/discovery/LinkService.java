package ai.wanaku.core.service.discovery;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ai.wanaku.api.types.management.Service;
import java.util.Map;
import org.jboss.resteasy.reactive.RestPath;

@Path("/api/v1/management/targets")
public interface LinkService {

    @Path("/tools/link")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    void toolsLink(@QueryParam("service") String service, @QueryParam("port") int port);

    @Path("/tools/unlink")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    void toolsUnlink(@QueryParam("service") String service);

    @Path("/tools/list")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    Map<String, Service> toolsList();

    @Path("/tools/configure/{service}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    Response toolsConfigure(@RestPath("service") String service, @QueryParam("option") String option, @QueryParam("value") String value);

    @Path("/resources/link")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    void resourcesLink(@QueryParam("service") String service, @QueryParam("port") int port);

    @Path("/resources/unlink")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    void resourcesUnlink(@QueryParam("service") String service);

    @Path("/resources/list")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    Map<String, Service> resourcesList();

    @Path("/resources/configure/{service}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    Response resourcesConfigure(@RestPath("service") String service, @QueryParam("option") String option, @QueryParam("value") String value);
}
