package com.intellij.spring.webflow.model.converters;

import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.spring.webflow.model.xml.Identified;
import com.intellij.spring.webflow.model.xml.WebflowDomModelManager;
import com.intellij.spring.webflow.model.xml.WebflowModel;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * User: plt
 */
public class IdentifiedStateConverter extends ResolvingConverter<Object> {

  public Object fromString(@Nullable @NonNls String s, ConvertContext context) {
    if(s == null) return null;

    if (s.contains(WebflowUtil.WEBFLOW_EL_PREFIX)) return s;

    WebflowModel webflowModel = getWebflowModel(context);
    if (webflowModel == null) return null;

    Flow flow = webflowModel.getFlow();
    List<Identified> identifiedList = WebflowUtil.getAllIdentified(flow);
    for (Identified identified : identifiedList) {
      if(s.equals(identified.getId().getStringValue())) {
        return identified;
      }
    }
    return null;
  }

  @Nullable
  private static WebflowModel getWebflowModel(ConvertContext context) {
    XmlFile xmlFile = context.getFile();
    return WebflowDomModelManager.getInstance(xmlFile.getProject()).getWebflowModel(xmlFile);
  }

  public String toString(@Nullable Object identified, ConvertContext context) {
    if (identified instanceof Identified) {
      return ((Identified)identified).getId().getStringValue();
    }
    return null;
  }

  @NotNull
  public Collection<? extends Object> getVariants(ConvertContext context) {
    return WebflowUtil.getAllIdentified(getWebflowModel(context).getFlow());
  }
}
