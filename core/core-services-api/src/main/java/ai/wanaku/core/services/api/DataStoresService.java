package ai.wanaku.core.services.api;

import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.DataStore;
import ai.wanaku.capabilities.sdk.api.types.WanakuResponse;
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
 * Service interface for DataStore operations via REST API.
 * All endpoints are available under the {@code /api/v1/data-store} base path.
 */
@Path("/api/v1/data-store")
public interface DataStoresService {

    /**
     * Add a new data store entry.
     * HTTP: POST /api/v1/data-store
     *
     * @param dataStore the data store to add
     * @return response with the created data store
     * @throws WanakuException if addition fails
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<DataStore> add(DataStore dataStore) throws WanakuException;

    /**
     * List all data stores, optionally filtered by label expression.
     * HTTP: GET /api/v1/data-store
     *
     * @param labelFilter optional label expression to filter data stores by labels
     * @return response with list of data stores
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<List<DataStore>> list(@QueryParam("labelFilter") String labelFilter);

    /**
     * List all data stores without filtering.
     *
     * @return response with list of all data stores
     */
    default WanakuResponse<List<DataStore>> list() {
        return list(null);
    }

    /**
     * Get a data store by ID.
     * HTTP: GET /api/v1/data-store/{id}
     *
     * @param id the ID of the data store
     * @return response with the data store
     */
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<DataStore> getById(@PathParam("id") String id);

    /**
     * Get data stores by name.
     * HTTP: GET /api/v1/data-store/by-name?name={name}
     *
     * @param name the name of the data stores
     * @return response with list of data stores
     */
    @Path("/by-name")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    WanakuResponse<List<DataStore>> getByName(@QueryParam("name") String name);

    /**
     * Remove a data store by ID.
     * HTTP: DELETE /api/v1/data-store/{id}
     *
     * @param id the ID of the data store to remove
     * @return HTTP response
     */
    @Path("/{id}")
    @DELETE
    Response remove(@PathParam("id") String id);

    /**
     * Remove data stores by name.
     * HTTP: DELETE /api/v1/data-store/by-name?name={name}
     *
     * @param name the name of the data stores to remove
     * @return HTTP response
     */
    @Path("/by-name")
    @DELETE
    Response removeByName(@QueryParam("name") String name);

    /**
     * Remove data stores matching a label expression.
     * HTTP: DELETE /api/v1/data-store/by-label?labelExpression={expr}
     *
     * @param labelExpression the label expression to match data stores for removal
     * @return response with count of removed data stores
     * @throws WanakuException if removal fails
     */
    @Path("/by-label")
    @DELETE
    WanakuResponse<Integer> removeIf(@QueryParam("labelExpression") String labelExpression) throws WanakuException;

    /**
     * Update an existing data store entry.
     * HTTP: PUT /api/v1/data-store
     *
     * @param dataStore the data store to update
     * @return HTTP response
     * @throws WanakuException if update fails
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(DataStore dataStore) throws WanakuException;
}
