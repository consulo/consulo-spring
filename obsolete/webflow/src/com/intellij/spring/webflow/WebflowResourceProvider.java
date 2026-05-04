/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.webflow;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.spring.webflow.constants.WebflowConstants;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class WebflowResourceProvider implements StandardResourceProvider{
  public void registerResources(ResourceRegistrar registrar) {
    addResource(WebflowConstants.WEBFLOW_1_0_SCHEMA, "/resources/schemas/spring-webflow-1.0.xsd", registrar);
    addResource(WebflowConstants.WEBFLOW_2_0_SCHEMA, "/resources/schemas/spring-webflow-2.0.xsd", registrar);

   addResource(WebflowConstants.WEBFLOW_CONFIG_1_0_SCHEMA, "/resources/schemas/spring-webflow-config-1.0.xsd", registrar);
   addResource(WebflowConstants.WEBFLOW_CONFIG_2_0_SCHEMA, "/resources/schemas/spring-webflow-config-2.0.xsd", registrar);
  }


  private static void addResource(@NonNls String url, @NonNls String file, ResourceRegistrar registrar) {
    registrar.addStdResource(url, file, WebflowApplicationComponent.class);
  }

}
