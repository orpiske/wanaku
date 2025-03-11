package ai.wanaku.routing.service;

import ai.wanaku.core.services.config.WanakuRoutingConfig;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ai.wanaku.core.exchange.ParsedToolInvokeRequest;
import ai.wanaku.core.exchange.ToolInvokeRequest;
import ai.wanaku.core.services.routing.Client;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DuckDuckGoClient implements Client {
    private static final Logger LOG = Logger.getLogger(DuckDuckGoClient.class);

    @Inject
    WanakuRoutingConfig config;

    private final ProducerTemplate producer;

    public DuckDuckGoClient(ProducerTemplate producer) {
        this.producer = producer;
    }

    @Override
    public Object exchange(ToolInvokeRequest request) {
        producer.start();

        String baseUri = config.baseUri();
        ParsedToolInvokeRequest parsedRequest = ParsedToolInvokeRequest.parseRequest(baseUri, request);

        LOG.infof("Invoking tool at URI: %s", parsedRequest.uri());

        String s = producer.requestBody(parsedRequest.uri(), parsedRequest.body(), String.class);
        return s;
    }
}