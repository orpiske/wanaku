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

package ai.wanaku.backend.proxies;

import ai.wanaku.backend.service.support.ServiceResolver;
import ai.wanaku.backend.support.ProvisioningReference;
import ai.wanaku.capabilities.sdk.api.types.ToolReference;
import ai.wanaku.capabilities.sdk.api.types.io.ToolPayload;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceTarget;
import ai.wanaku.capabilities.sdk.api.types.providers.ServiceType;
import ai.wanaku.core.mcp.common.ToolExecutor;
import org.jboss.logging.Logger;

/**
 * A proxy class for invoking tools via gRPC.
 * <p>
 * This proxy is responsible for provisioning tool configurations and
 * providing access to a tool executor. The actual tool execution logic
 * is delegated to {@link InvokerToolExecutor} through composition,
 * separating proxy management from execution concerns.
 * <p>
 * This class extends {@link GrpcProxy} to inherit common gRPC functionality
 * such as service resolution, channel management, and provisioning operations.
 * It focuses solely on tool-specific concerns while delegating infrastructure
 * concerns to the base class.
 */
public class InvokerProxy extends GrpcProxy implements ToolsProxy {
    private static final Logger LOG = Logger.getLogger(InvokerProxy.class);

    private final ToolExecutor executor;

    /**
     * Creates a new InvokerProxy with the specified service resolver.
     *
     * @param serviceResolver the resolver for locating tool services
     */
    public InvokerProxy(ServiceResolver serviceResolver) {
        super(serviceResolver);
        this.executor = new InvokerToolExecutor(serviceResolver);
    }

    @Override
    public ToolExecutor getExecutor() {
        return executor;
    }

    @Override
    public ProvisioningReference provision(ToolPayload toolPayload) {
        ToolReference toolReference = toolPayload.getPayload();

        LOG.debugf("Provisioning tool: %s (type: %s)", toolReference.getName(), toolReference.getType());

        ServiceTarget service = resolveService(toolReference.getType(), ServiceType.TOOL_INVOKER);

        return provision(
                toolReference.getName(), toolPayload.getConfigurationData(), toolPayload.getSecretsData(), service);
    }
}
