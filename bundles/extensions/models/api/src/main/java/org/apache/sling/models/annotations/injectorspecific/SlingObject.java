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
package org.apache.sling.models.annotations.injectorspecific;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

/**
 * Injects common Sling objects that can be derived from either a SlingHttpServletRequest, a ResourceResolver or a
 * Resource.
 * The injection is class-based.
 * <p>
 * Supports the following objects:
 * <table>
 * <tr>
 * <th style="text-align:left">Class</th>
 * <th style="text-align:left">Description</th>
 * <th style="text-align:center">Request</th>
 * <th style="text-align:center">ResourceResolver</th>
 * <th style="text-align:center">Resource</th>
 * </tr>
 * <tr style="background-color:#eee">
 * <td>{@link ResourceResolver}</td>
 * <td>Resource resolver</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X</td>
 * </tr>
 * <tr>
 * <td>{@link Resource}</td>
 * <td>Resource</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X*</td>
 * <td style="text-align:center">X</td>
 * </tr>
 * <tr style="background-color:#eee">
 * <td>{@link SlingHttpServletRequest}</td>
 * <td>Sling request</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X*</td>
 * <td style="text-align:center">X*</td>
 * </tr>
 * <tr>
 * <td>{@link SlingHttpServletResponse}</td>
 * <td>Sling response</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X*</td>
 * <td style="text-align:center">X*</td>
 * </tr>
 * <tr style="background-color:#eee">
 * <td>{@link SlingScriptHelper}</td>
 * <td>Sling script helper</td>
 * <td style="text-align:center">X</td>
 * <td style="text-align:center">X*</td>
 * <td style="text-align:center">X*</td>
 * </tr>
 * </table>
 * <p>
 * In case of X* the class cannot be derived from the adaptable, but is derived from the request of the current thread
 * set by a servlet filter. If the current thread is not associated with a request nothing is injected.
 * </p>
 */
@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@InjectAnnotation
@Source("sling-object")
public @interface SlingObject {

  /**
   * If set to true, the model can be instantiated even if there is no request attribute
   * with the given name found.
   * Default = false.
   */
  boolean optional() default false;

}