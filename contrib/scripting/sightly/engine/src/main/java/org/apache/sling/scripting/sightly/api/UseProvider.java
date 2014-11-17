/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/

package org.apache.sling.scripting.sightly.api;

import javax.script.Bindings;

/**
 * Provides instances for the use API. Providers are tried in the order
 * of their priority until one is found which can provide a non-null instance
 */
public interface UseProvider extends Comparable<UseProvider> {

    int DEFAULT_PRIORITY = 0;

    /**
     * Provide an instance based on the given identifier
     * @param identifier the identifier of the dependency
     * @param renderContext the current rendering context
     * @param arguments Specific arguments provided by the use plugin
     * @return a container with the instance that corresponds to the identifier. If the identifier cannot be
     * handled by this provider, a failed outcome is returned
     */
    ProviderOutcome provide(String identifier, RenderContext renderContext, Bindings arguments);

    /**
     * The priority with which this use provider should be selected. Use 0 if you don't care
     * @return the (possibly negative) priority of this provider
     */
    int priority();

}