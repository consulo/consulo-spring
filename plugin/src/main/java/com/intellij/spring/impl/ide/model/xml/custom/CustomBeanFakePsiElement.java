/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.model.xml.custom;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.impl.psi.RenameableFakePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;

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

  @Override
  public XmlTag getParent() {
    return myBean.getXmlTag();
  }

  @Override
  @RequiredReadAction
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

  @Override
  public String getTypeName() {
    return SpringLocalize.springBean().get();
  }

  @Override
  @RequiredWriteAction
  public PsiElement setName(@Nonnull String name) throws IncorrectOperationException
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

  @Override
  public Image getIcon() {
    return SpringImplIconGroup.springbean();
  }
}
