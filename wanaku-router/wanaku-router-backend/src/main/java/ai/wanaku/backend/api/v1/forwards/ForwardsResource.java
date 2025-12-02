package ai.wanaku.backend.api.v1.forwards;

import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.ForwardReference;
import ai.wanaku.capabilities.sdk.api.types.WanakuResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * JAX-RS REST resource implementation for forward management endpoints.
 * <p>
 * This class implements the {@code /api/v1/forwards} endpoints for managing
 * forward references in the Wanaku router.
 */
@ApplicationScoped
@Path("/api/v1/forwards")
public class ForwardsResource {
    @Inject
    ForwardsBean forwardsBean;

    /**
     * Registers a new forward reference.
     * <p>
     * HTTP: POST /api/v1/forwards
     *
     * @param reference the forward reference to register
     * @return HTTP 200 OK if registered successfully
     * @throws WanakuException if registration fails
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addForward(ForwardReference reference) throws WanakuException {
        forwardsBean.forward(reference);
        return Response.ok().build();
    }

    /**
     * Removes a forward reference.
     * <p>
     * HTTP: DELETE /api/v1/forwards
     *
     * @param reference the forward reference to remove
     * @return HTTP 200 OK if removed, HTTP 404 NOT FOUND if not found
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeForward(ForwardReference reference) {
        int deleteCount = forwardsBean.remove(reference);
        if (deleteCount > 0) {
            return Response.ok().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Lists all registered forward references.
     * <p>
     * HTTP: GET /api/v1/forwards
     *
     * @param labelFilter optional label expression to filter forwards by labels
     * @return a response containing a list of all forward references
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<WanakuResponse<List<ForwardReference>>> listForwards(
            @jakarta.ws.rs.QueryParam("labelFilter") String labelFilter) {
        return RestResponse.ok(new WanakuResponse<>(forwardsBean.listForwards(labelFilter)));
    }

    /**
     * Retrieves a forward reference by name.
     * <p>
     * HTTP: GET /api/v1/forwards/{name}
     *
     * @param name the name of the forward to retrieve
     * @return a response containing the forward reference
     * @throws WanakuException if the forward is not found or retrieval fails
     */
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<ForwardReference> getByName(@PathParam("name") String name) throws WanakuException {
        ForwardReference forward = forwardsBean.getByName(name);
        if (forward == null) {
            throw new WanakuException("Forward not found: " + name);
        }
        return new WanakuResponse<>(forward);
    }

    /**
     * Updates an existing forward reference.
     * <p>
     * HTTP: PUT /api/v1/forwards
     *
     * @param resource the updated forward reference
     * @return HTTP 200 OK if updated successfully
     * @throws WanakuException if update fails
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(ForwardReference resource) throws WanakuException {
        forwardsBean.update(resource);
        return Response.ok().build();
    }
}
