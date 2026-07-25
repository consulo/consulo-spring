package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.localize.LocalizeValue;
import consulo.xml.dom.DomElement;
import jakarta.annotation.Nullable;

public class SpringBeanGenerateProvider extends BasicSpringDomGenerateProvider<SpringBean> {
  public SpringBeanGenerateProvider(LocalizeValue description, String template) {
    super(description, SpringBean.class, template);
  }

  @Nullable
  @Override
  protected DomElement getElementToNavigate(SpringBean springBean) {
    return null;
  }
}
