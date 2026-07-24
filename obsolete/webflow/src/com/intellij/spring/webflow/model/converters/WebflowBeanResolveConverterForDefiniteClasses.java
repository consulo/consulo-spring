package com.intellij.spring.webflow.model.converters;

import com.intellij.spring.model.converters.SpringBeanResolveConverterForDefiniteClasses;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.SpringModel;
import com.intellij.spring.SpringManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomFileElement;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class WebflowBeanResolveConverterForDefiniteClasses extends SpringBeanResolveConverterForDefiniteClasses {

  protected SpringModel getSpringModel(ConvertContext context) {
    SpringModel model = super.getSpringModel(context);

    return model == null ? getModel(context.getModule()) : model;
  }

  @Nullable
  protected Beans getBeans(GenericDomValue<SpringBeanPointer> genericDomValue) {
    Beans beans = super.getBeans(genericDomValue);

    if (beans != null) return beans;

    SpringModel springModel = getModel(genericDomValue.getModule());
    if (springModel == null) return null;

    List<DomFileElement<Beans>> elementList = springModel.getRoots();
    if (elementList.size() > 0) {
      return elementList.get(0).getRootElement();
    }
    return null;
  }

  @Nullable
  private static SpringModel getModel(@Nullable Module module) {
    return module == null ? null : SpringManager.getInstance(module.getProject()).getCombinedModel(module);
  }
}
