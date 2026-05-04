package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.model.xml.If;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

/**
 * User: plt
 */
public abstract class WebflowIfEdge extends WebflowBasicEdge<GenericAttributeValue<Object>> {
  private final If myIfElement;

  public WebflowIfEdge(final WebflowNode source,
                       final WebflowNode target,
                       final If ifElement,
                       final GenericAttributeValue<Object> identifying) {
    super(source, target, identifying);
    myIfElement = ifElement;
  }

  public If getIfElement() {
    return myIfElement;
  }

  public static class Then extends WebflowIfEdge {

    public Then(final WebflowNode source, final WebflowNode target, final If ifElement, final GenericAttributeValue<Object> identifying) {
      super(source, target, ifElement, identifying);
    }

    @NotNull
    public String getName() {
      final GenericAttributeValue<String> value = getIfElement().getTest();

      if (value.isValid() && DomUtil.hasXml(value)) {
        final String stringValue = value.getStringValue();
        if (stringValue != null) return stringValue;
      }

      return "";
    }
  }

  public static class Else extends WebflowIfEdge {
    public Else(final WebflowNode source, final WebflowNode target, final If ifElement, final GenericAttributeValue<Object> identifying) {
      super(source, target, ifElement, identifying);
    }

    @NotNull
    public String getName() {
        return "";
    }
  }
}

