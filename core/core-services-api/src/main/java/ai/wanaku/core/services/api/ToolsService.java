package ai.wanaku.core.services.api;

import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;
import ai.wanaku.capabilities.sdk.api.types.WanakuResponse;
import ai.wanaku.capabilities.sdk.api.types.io.ToolPayload;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * JAX-RS service interface for managing tool capabilities in the Wanaku system.
 * This service provides REST endpoints for registering, listing, updating, and removing
 * tool capabilities. Tools are executable capabilities that can be invoked by AI agents
 * to perform specific tasks.
 * All endpoints are available under the {@code /api/v1/tools} base path.
 */
@Path("/api/v1/tools")
public interface ToolsService {

    /**
     * Registers a new tool capability in the system.
     * HTTP: POST /api/v1/tools
     *
     * @param toolReference the tool reference containing tool metadata
     * @return a {@link WanakuResponse} containing the registered tool reference
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<ToolReference> add(ToolReference toolReference);

    /**
     * Registers a new tool capability with configuration and secrets payload.
     * This endpoint allows registering a tool along with its provisioning data,
     * including configuration settings and secrets required for tool operation.
     * HTTP: POST /api/v1/tools/with-payload
     *
     * @param resource the tool payload containing the tool reference and provisioning data
     * @return a {@link WanakuResponse} containing the registered tool reference
     * @throws WanakuException if registration fails
     */
    @Path("/with-payload")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<ToolReference> addWithPayload(ToolPayload resource) throws WanakuException;

    /**
     * Lists all registered tool capabilities.
     * HTTP: GET /api/v1/tools
     *
     * @param labelFilter optional label expression to filter tools by labels
     * @return a {@link WanakuResponse} containing a list of all tool references
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<List<ToolReference>> list(@QueryParam("labelFilter") String labelFilter);

    /**
     * Lists all registered tool capabilities without filtering.
     *
     * @return a {@link WanakuResponse} containing a list of all tool references
     */
    default WanakuResponse<List<ToolReference>> list() {
        return list(null);
    }

    /**
     * Removes a tool capability from the system.
     * HTTP: DELETE /api/v1/tools?tool={name}
     *
     * @param tool the name of the tool to remove
     * @return a {@link Response} indicating the result of the removal operation
     */
    @DELETE
    Response remove(@QueryParam("tool") String tool);

    /**
     * Updates an existing tool capability.
     * HTTP: PUT /api/v1/tools
     *
     * @param resource the updated tool reference
     * @return a {@link Response} indicating the result of the update operation
     * @throws WanakuException if the update fails
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(ToolReference resource) throws WanakuException;

    /**
     * Retrieves a tool capability by its name.
     * HTTP: GET /api/v1/tools/{name}
     *
     * @param name the name of the tool to retrieve
     * @return a {@link WanakuResponse} containing the tool reference
     * @throws WanakuException if the tool is not found
     */
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<ToolReference> getByName(@PathParam("name") String name) throws WanakuException;

    /**
     * Removes tool capabilities from the system that match a label expression.
     * HTTP: DELETE /api/v1/tools?labelExpression={expr}
     *
     * @param labelExpression the label expression to filter tools for removal
     * @return a {@link WanakuResponse} containing the number of tools removed
     * @throws WanakuException if the removal fails
     */
    @Path("/by-label")
    @DELETE
    WanakuResponse<Integer> removeIf(@QueryParam("labelExpression") String labelExpression) throws WanakuException;
}
