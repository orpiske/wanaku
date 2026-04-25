package ai.wanaku.backend.mcp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletionStage;
import org.jboss.logging.Logger;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RoutingExchange;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class McpResource {
    private static final Logger LOG = Logger.getLogger(McpResource.class);

    private final Vertx vertx;
    private final WebClient client;

    @Inject
    McpResource(Vertx vertx) {
        this.vertx = vertx;
        this.client = WebClient.create(vertx);
    }

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

    @Route(path = "/api/v1/mcp/route/:name", methods = Route.HttpMethod.GET)
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
}
