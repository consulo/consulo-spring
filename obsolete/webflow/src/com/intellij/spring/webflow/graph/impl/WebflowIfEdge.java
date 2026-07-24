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

  public WebflowIfEdge(WebflowNode source,
                       WebflowNode target,
                       If ifElement,
                       GenericAttributeValue<Object> identifying) {
    super(source, target, identifying);
    myIfElement = ifElement;
  }

  public If getIfElement() {
    return myIfElement;
  }

  public static class Then extends WebflowIfEdge {

    public Then(WebflowNode source, WebflowNode target, If ifElement, GenericAttributeValue<Object> identifying) {
      super(source, target, ifElement, identifying);
    }

    @NotNull
    public String getName() {
      GenericAttributeValue<String> value = getIfElement().getTest();

      if (value.isValid() && DomUtil.hasXml(value)) {
        String stringValue = value.getStringValue();
        if (stringValue != null) return stringValue;
      }

      return "";
    }
  }

  public static class Else extends WebflowIfEdge {
    public Else(WebflowNode source, WebflowNode target, If ifElement, GenericAttributeValue<Object> identifying) {
      super(source, target, ifElement, identifying);
    }

    @NotNull
    public String getName() {
        return "";
    }
  }
}

