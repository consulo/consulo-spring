package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.model.xml.Transition;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

/**
 * User: plt
 */
public class WebflowTransitionEdge extends WebflowBasicEdge<Transition> {
  public WebflowTransitionEdge(final WebflowNode source, final WebflowNode target, final Transition transition) {
    super(source, target, transition);
  }

  @NotNull
  public String getName() {
    if (isOnEventTransition()) {
      final String value = getIdentifyingElement().getOn().getStringValue();
      if (value != null) return value;
    } else {
      final PsiClass psiClass = getIdentifyingElement().getOnException().getValue();
      if (psiClass != null) {
        final String name = psiClass.getName();
        if (name != null) {
          return name;
        }
      } else {
        final String value = getIdentifyingElement().getOnException().getStringValue();
        if (value != null) return value;
      }
    }

    return "";
  }

  public boolean isOnEventTransition() {
    return DomUtil.hasXml(getIdentifyingElement().getOn());
  }
}
