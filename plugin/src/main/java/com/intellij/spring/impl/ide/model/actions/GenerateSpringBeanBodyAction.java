package com.intellij.spring.impl.ide.model.actions;

import com.intellij.spring.impl.ide.SpringIcons;
import consulo.xml.util.xml.ui.actions.generate.GenerateDomElementProvider;
import consulo.ui.image.Image;

public class GenerateSpringBeanBodyAction extends GenerateSpringDomElementAction {
  public GenerateSpringBeanBodyAction(GenerateDomElementProvider provider) {
    this(provider, SpringIcons.SPRING_BEAN_PROPERTY_ICON);
  }

  public GenerateSpringBeanBodyAction(GenerateDomElementProvider provider, Image icon) {
    super(provider, icon);
  }
}
