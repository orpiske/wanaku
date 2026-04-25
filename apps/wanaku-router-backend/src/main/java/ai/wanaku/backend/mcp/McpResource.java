package ai.wanaku.backend.mcp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;
import io.quarkus.vertx.web.Body;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.RoutingExchange;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
@RouteBase(path = "/api/v1/mcp")
public class McpResource {
    private static final Logger LOG = Logger.getLogger(McpResource.class);

    @Inject
    ManagedExecutor managedExecutor;

    //    @Inject
    //    McpResource(Vertx vertx) {
    //        this.vertx = vertx;
    //        this.client = WebClient.create(vertx);
    //    }

    private JsonObject handleBody(JsonObject body) {
        LOG.infof("Calling %s", body);

        var ret = new JsonObject();
        return ret.put("test", "value");
    }

    private JsonObject toJson(Buffer buffer) {
        int length = buffer.length();
        LOG.infof("Bytes in buffer %d", length);
        if (length == 0) {
            return new JsonObject();
        }

        return buffer.toJsonObject();
    }

    private String toBody(JsonObject ret) {
        LOG.infof("Returning %s", ret.toString());
        return ret.toString();
    }

    @Route(path = "/route/:name", methods = Route.HttpMethod.GET)
    public void get(@Param String name, RoutingExchange ex) {
        LOG.infof("Looking for route %s", name);
        HttpServerRequest request = ex.request();
        CompletionStage<Buffer> body = request.body().toCompletionStage();

        HttpServerResponse response = ex.response();

        body.thenApply(this::toJson)
                .thenApply(this::handleBody)
                .thenApply(this::toBody)
                .thenAccept(b -> response.setStatusCode(200)
                        .putHeader("Content-Type", "text/event-stream")
                        .end((b)));
    }

    @Route(path = "/route/:name", methods = Route.HttpMethod.POST)
    public Uni<JsonObject> initialize(@Body String body, @Param String name, RoutingExchange ex) {
        LOG.infof("Looking for route %s with data %s", name, body);

        //        HttpServerResponse response = ex.response();

        return Uni.createFrom()
                .item(new JsonObject(body))
                //                .emitOn(Infrastructure.getDefaultExecutor())
                .invoke(this::handleBody)
                .invoke(this::toBody)
                .invoke(res ->
                        ex.ok().putHeader("Content-Type", "text/event-stream").end(res.toString()));
    }
}
