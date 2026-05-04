package com.intellij.spring.web;

import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.javaee.model.xml.web.Servlet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
*/
public abstract class ServletFileSet extends SpringFileSet {

  public ServletFileSet(@NonNls @NotNull String id, @NotNull String name, @NotNull final SpringFacetConfiguration parent) {
    super(id, name, parent);
  }

  @Nullable
  public abstract Servlet getServlet();
}
