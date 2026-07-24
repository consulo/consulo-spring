/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.model.xml.custom;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringIcons;
import consulo.language.impl.psi.RenameableFakePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

/**
 * @author peter
*/
public class CustomBeanFakePsiElement extends RenameableFakePsiElement
{
  private final CustomNamespaceSpringBean myBean;

  public CustomBeanFakePsiElement(CustomNamespaceSpringBean bean) {
    super(bean.getContainingFile());
    myBean = bean;
  }

  public XmlTag getParent() {
    return myBean.getXmlTag();
  }

  @Override
  public String getName() {
    return myBean.getBeanName();
  }

  public CustomNamespaceSpringBean getBean() {
    return myBean;
  }

  @Override
  public PsiElement getNavigationElement() {
    return getParent();
  }

  public String getTypeName() {
    return SpringBundle.message("spring.bean");
  }

  @Override
    public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
  {
    XmlAttribute idAttribute = myBean.getIdAttribute();
    if (idAttribute != null) {
      idAttribute.setValue(name);
    }
    return super.setName(name);
  }

  @Override
  public boolean isEquivalentTo(PsiElement another) {
    if (another instanceof CustomBeanFakePsiElement) {
      CustomBeanFakePsiElement element = (CustomBeanFakePsiElement)another;
      return element.getBean().equals(getBean());
    }
    return false;
  }

  public Image getIcon() {
    return SpringIcons.SPRING_BEAN_ICON;
  }
}
