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

package org.wanaku.server.quarkus.api.v1.tools;

import java.io.File;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.wanaku.api.resolvers.ToolsResolver;
import org.wanaku.api.types.ToolReference;
import org.wanaku.core.util.IndexHelper;

@ApplicationScoped
public class ToolsBean {
    @Inject
    ToolsResolver toolsResolver;

    public void add(ToolReference mcpResource) {
        File indexFile = toolsResolver.indexLocation();
        try {
            List<ToolReference> toolReferences = IndexHelper.loadToolsIndex(indexFile);
            toolReferences.add(mcpResource);
            IndexHelper.saveToolsIndex(indexFile, toolReferences);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<ToolReference> list() {
        File indexFile = toolsResolver.indexLocation();
        try {
            return IndexHelper.loadToolsIndex(indexFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
