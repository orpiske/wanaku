package ai.wanaku.backend.api.v1.datastores;

import ai.wanaku.capabilities.sdk.api.exceptions.DataStoreResourceNotFoundException;
import ai.wanaku.capabilities.sdk.api.exceptions.WanakuException;
import ai.wanaku.capabilities.sdk.api.types.DataStore;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * REST API resource for managing DataStore entries.
 * Base path: /api/v1/data-store
 */
@ApplicationScoped
@Path("/api/v1/data-store")
public class DataStoresResource {
    private static final Logger LOG = Logger.getLogger(DataStoresResource.class);

    @Inject
    DataStoresBean dataStoresBean;

    /**
     * Add a new data store entry.
     * <p>
     * HTTP: POST /api/v1/data-store
     *
     * @param dataStore the data store to add
     * @return response with the created data store
     * @throws WanakuException if addition fails
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<DataStore> add(DataStore dataStore) throws WanakuException {
        LOG.debugf("REST: Adding data store: %s", dataStore);
        DataStore result = dataStoresBean.add(dataStore);
        return new WanakuResponse<>(result);
    }

    /**
     * Update an existing data store entry.
     * <p>
     * HTTP: PUT /api/v1/data-store
     *
     * @param dataStore the data store to update (must include ID)
     * @return HTTP 200 if updated successfully
     * @throws WanakuException if update fails
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(DataStore dataStore) throws WanakuException {
        LOG.debugf("REST: Updating data store: %s", dataStore);
        dataStoresBean.update(dataStore);
        return Response.ok().build();
    }

    /**
     * List all data stores, optionally filtered by label expression.
     * <p>
     * HTTP: GET /api/v1/data-store
     * HTTP: GET /api/v1/data-store?labelFilter={expression}
     *
     * @param labelFilter optional label expression to filter data stores
     * @return response with list of data stores
     * @throws WanakuException if listing or label expression parsing fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<List<DataStore>> list(@QueryParam("labelFilter") String labelFilter) throws WanakuException {
        if (labelFilter != null && !labelFilter.isBlank()) {
            LOG.debugf("REST: Listing data stores with label filter: %s", labelFilter);
        } else {
            LOG.debug("REST: Listing all data stores");
        }
        List<DataStore> dataStores = dataStoresBean.list(labelFilter);
        return new WanakuResponse<>(dataStores);
    }

    /**
     * Get a data store by ID.
     * <p>
     * HTTP: GET /api/v1/data-store/{id}
     *
     * @param id the ID of the data store
     * @return response with the requested data store
     * @throws WanakuException if retrieval fails
     */
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<DataStore> getById(@PathParam("id") String id) throws WanakuException {
        LOG.debugf("REST: Getting data store by ID: %s", id);
        DataStore dataStore = dataStoresBean.findById(id);
        if (dataStore == null) {
            throw new DataStoreResourceNotFoundException("Data store not found with ID: " + id);
        }
        return new WanakuResponse<>(dataStore);
    }

    /**
     * Get data stores by name.
     * <p>
     * HTTP: GET /api/v1/data-store/by-name?name={name}
     *
     * @param name the name of the data stores
     * @return response with list of data stores
     * @throws WanakuException if retrieval fails
     */
    @Path("/by-name")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<List<DataStore>> getByName(@QueryParam("name") String name) throws WanakuException {
        if (name == null || name.isEmpty()) {
            throw new WanakuException("'name' query parameter must be provided");
        }
        LOG.debugf("REST: Getting data stores by name: %s", name);
        List<DataStore> dataStores = dataStoresBean.findByName(name);
        if (dataStores == null || dataStores.isEmpty()) {
            throw new DataStoreResourceNotFoundException("Data store not found with name: " + name);
        }
        return new WanakuResponse<>(dataStores);
    }

    /**
     * Remove a data store by ID.
     * <p>
     * HTTP: DELETE /api/v1/data-store/{id}
     *
     * @param id the ID of the data store to remove
     * @return HTTP 200 if removed, 404 if not found
     * @throws WanakuException if removal fails
     */
    @Path("/{id}")
    @DELETE
    public Response remove(@PathParam("id") String id) throws WanakuException {
        LOG.debugf("REST: Removing data store by ID: %s", id);
        int deleteCount = dataStoresBean.removeById(id);
        if (deleteCount > 0) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Remove data stores by name.
     * <p>
     * HTTP: DELETE /api/v1/data-store/by-name?name={name}
     *
     * @param name the name of the data stores to remove
     * @return HTTP 200 if removed, 404 if not found
     * @throws WanakuException if removal fails
     */
    @Path("/by-name")
    @DELETE
    public Response removeByName(@QueryParam("name") String name) throws WanakuException {
        if (name == null || name.isEmpty()) {
            throw new WanakuException("'name' query parameter must be provided");
        }
        LOG.debugf("REST: Removing data stores by name: %s", name);
        int deleteCount = dataStoresBean.remove(name);
        if (deleteCount > 0) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Remove data stores matching a label expression.
     * <p>
     * HTTP: DELETE /api/v1/data-store/by-label?labelExpression={expression}
     *
     * @param labelExpression the label expression to match data stores for removal
     * @return response with count of removed data stores
     * @throws WanakuException if label expression is invalid or removal fails
     */
    @Path("/by-label")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public WanakuResponse<Integer> removeIf(@QueryParam("labelExpression") String labelExpression)
            throws WanakuException {
        LOG.debugf("REST: Removing data stores by label expression: %s", labelExpression);
        int removed = dataStoresBean.removeIf(labelExpression);
        return new WanakuResponse<>(removed);
    }
}
