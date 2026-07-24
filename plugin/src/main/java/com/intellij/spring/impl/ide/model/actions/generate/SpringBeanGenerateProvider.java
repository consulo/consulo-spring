package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.xml.dom.DomElement;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;

public class SpringBeanGenerateProvider extends BasicSpringDomGenerateProvider<SpringBean> {

  public SpringBeanGenerateProvider(String description, @NonNls String template) {
    super(description, SpringBean.class, template);
  }

  @Nullable
  protected DomElement getElementToNavigate(SpringBean springBean) {
    return null;
  }
}
