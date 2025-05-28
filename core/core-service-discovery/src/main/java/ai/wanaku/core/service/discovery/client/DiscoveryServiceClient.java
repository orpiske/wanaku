package ai.wanaku.core.service.discovery.client;

import ai.wanaku.core.mcp.providers.ServiceTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class DiscoveryServiceClient implements DiscoveryService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl; // e.g., "http://localhost:8080"
    private final String serviceBasePath = "/api/v1/management/targets";

    public DiscoveryServiceClient(String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        this.baseUrl = baseUrl != null && baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private Response executePost(String operationPath, ServiceTarget serviceTarget) {
        try {
            String jsonRequestBody = objectMapper.writeValueAsString(serviceTarget);
            URI uri = URI.create(this.baseUrl + this.serviceBasePath + operationPath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Accept", MediaType.WILDCARD)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequestBody))
                    .build();

            HttpResponse<String> httpResponse =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Convert java.net.http.HttpResponse to jakarta.ws.rs.core.Response
            Response.ResponseBuilder responseBuilder = Response.status(httpResponse.statusCode());

            String responseBody = httpResponse.body();
            if (responseBody != null) {
                responseBuilder.entity(responseBody);
            }

            // Copy headers from httpResponse to JAX-RS Response
            Map<String, List<String>> httpHeaders = httpResponse.headers().map();
            for (Map.Entry<String, List<String>> headerEntry : httpHeaders.entrySet()) {
                if (headerEntry.getKey() != null && headerEntry.getValue() != null) {
                    for (String headerValue : headerEntry.getValue()) {
                        responseBuilder.header(headerEntry.getKey(), headerValue);
                    }
                }
            }

            return responseBuilder.build();

        } catch (JsonProcessingException e) {
            // Error during request body serialization
            return Response.status(Response.Status.BAD_REQUEST) // Or INTERNAL_SERVER_ERROR on client side
                    .entity("Failed to serialize ServiceTarget to JSON: " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (IOException e) {
            // Error during HTTP request execution
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("HTTP request failed: " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InterruptedException e) {
            // Thread was interrupted
            Thread.currentThread().interrupt(); // Preserve interrupt status
            return Response.status(Response.Status.SERVICE_UNAVAILABLE) // Or a custom status
                    .entity("HTTP request interrupted: " + e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @Override
    public Response register(ServiceTarget serviceTarget) {
        return executePost("/register", serviceTarget);
    }

    @Override
    public Response deregister(ServiceTarget serviceTarget) {
        return executePost("/deregister", serviceTarget);
    }

    @Override
    public Response update(ServiceTarget serviceTarget) {
        return executePost("/update", serviceTarget);
    }
}