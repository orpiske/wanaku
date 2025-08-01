package ai.wanaku.backend.api.v1.user;

import io.quarkus.oidc.OidcConfigurationMetadata;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import org.keycloak.admin.client.Keycloak;

@ApplicationScoped
@Path("/api/v1/user")
public class UserResource {


    @Inject
    Keycloak keycloak;

    @Inject
    RoutingContext routingContext;

    @GET
    @Path("/account")
    public Response account() {

        return Response.ok(keycloak.realm("wanaku").users().get("fd7de19b-7ecf-404c-969f-66e73f29530c").toRepresentation()).build();
//
//        routingContext.response()
//                .setStatusCode(302)
//                .putHeader("Location", configMetadata.getIssuer() + "/account")
//                .end();
    }
}
