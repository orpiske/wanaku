/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wanaku.routers.camel.proxies.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;
import org.wanaku.api.types.McpTool;
import org.wanaku.api.types.McpToolContent;
import org.wanaku.api.types.McpToolStatus;
import org.wanaku.api.types.ToolReference;
import org.wanaku.routers.camel.proxies.ToolsProxy;

public class CamelEndpointProxy implements ToolsProxy {
    private static final Logger LOG = Logger.getLogger(CamelEndpointProxy.class);

    private final ProducerTemplate producer;


    public CamelEndpointProxy(CamelContext context) {
        this.producer = context.createProducerTemplate();
    }

    @Override
    public boolean canHandle(ToolReference toolReference) {
        return toolReference.getType().equals("http");
    }

    @Override
    public McpToolStatus call(McpTool tool, Map<String, Object> properties) {
        McpToolStatus status = new McpToolStatus();
        try {
            producer.start();

            String uri = tool.uri;

            for (var t : tool.inputSchema.properties) {
                Object o = properties.get(t.name);
                uri = uri.replace(String.format("{%s}", t.name), o.toString());
            }

            McpToolContent content = new McpToolContent();
            content.text = producer.requestBody(uri, null, String.class);
            content.type = "text";

            status.content = List.of(content);
            status.isError = false;
        } catch (Exception e) {
            LOG.errorf("Unable to call endpoint: %s", e.getMessage(), e);
            McpToolContent content = new McpToolContent();
            content.text = e.getMessage();
            content.type = "text";

            status.isError = true;
            status.content = List.of(content);
        } finally {
            producer.stop();
        }
        return status;
    }

    @Override
    public McpTool toMcpTool(ToolReference toolReference) {
        McpTool tool = new McpTool();

        tool.description = toolReference.getDescription();
        tool.name = toolReference.getName();
        tool.uri = toolReference.getUri();
        tool.type = toolReference.getType();
        tool.inputSchema = new McpTool.InputSchema();
        ToolReference.InputSchema inputSchema = toolReference.getInputSchema();
        if (inputSchema != null) {
            tool.inputSchema.type = inputSchema.getType();
            tool.inputSchema.properties = new ArrayList<>();

            for (var refProperty : inputSchema.getProperties().entrySet()) {
                McpTool.InputSchema.Property schemaProperty = new McpTool.InputSchema.Property();

                schemaProperty.name = refProperty.getKey();
                schemaProperty.type = refProperty.getValue().getType();
                schemaProperty.description = refProperty.getValue().getDescription();
                tool.inputSchema.properties.add(schemaProperty);
            }
        }
        return tool;
    }

    @Override
    public String name() {
        return "camel-endpoint";
    }
}
