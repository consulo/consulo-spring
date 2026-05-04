/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public interface SpringWebConstants {
  @NonNls String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";
  @NonNls String DISPATCHER_SERVLET_CLASS = "org.springframework.web.servlet.DispatcherServlet";
  @NonNls String APPLICATION_CONTEXT_XML = "applicationContext.xml";
  @NonNls String CONTEXT_LISTENER_CLASS = "org.springframework.web.context.ContextLoaderListener";
  @NonNls String WEB_INF = "/WEB-INF/";
  @NonNls String MVC_FORM_TLD = "http://www.springframework.org/tags/form";
}
