/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.custom;

import com.intellij.jam.model.common.ReadOnlyGenericValue;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.CustomBeanInfo;
import com.intellij.spring.impl.ide.CustomBeanRegistry;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.model.AbstractDomSpringBean;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.util.collection.ArrayUtil;
import consulo.xml.dom.GenericValue;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author peter
*/
public class CustomNamespaceSpringBean extends AbstractDomSpringBean implements CustomBean {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.model.xml.custom.CustomNamespaceSpringBean");
  private final CustomBeanInfo myInfo;
  private final consulo.module.Module myModule;
  private final XmlTag mySourceTag;
  private final PsiElement myFakePsi;
  private final CustomBeanWrapper myWrapper;
  private final XmlAttribute myIdAttribute;

  @RequiredReadAction
  public CustomNamespaceSpringBean(@Nonnull CustomBeanInfo info, consulo.module.Module module, @Nonnull CustomBeanWrapper wrapper) {
    myInfo = info;
    myModule = module;
    myWrapper = wrapper;
    XmlTag tag = wrapper.getXmlTag();
    if (tag == null) {
      LOG.error(String.valueOf(wrapper.getParent()));
    }
    tag = CustomBeanRegistry.getActualSourceTag(info, tag);
    mySourceTag = tag;

    myIdAttribute = mySourceTag.getAttribute(info.idAttribute);

    myFakePsi = new CustomBeanFakePsiElement(this);
  }

  @Nullable
  @Override
  public XmlAttribute getIdAttribute() {
    return myIdAttribute;
  }

  @Nullable
  @Override
  @RequiredReadAction
  public GenericValue<SpringBeanPointer> getFactoryBean() {
    String beanName = myInfo.factoryBeanName;
    if (beanName != null) {
      SpringModel model = SpringManager.getInstance(getPsiManager().getProject()).getSpringModelByFile((XmlFile)getContainingFile());
      if (model != null) {
        SpringBeanPointer beanPointer = SpringUtils.getBeanPointer(model, beanName);
        if (beanPointer != null) {
          return ReadOnlyGenericValue.getInstance(beanPointer);
        }
      }
    }
    return super.getFactoryBean();
  }

  @Nonnull
  @Override
  public CustomBeanWrapper getWrapper() {
    return myWrapper;
  }

  @Nullable
  @Override
  public GenericValue<PsiMethod> getFactoryMethod() {
    String name = myInfo.factoryMethodName;
    if (name != null) {
      PsiClass beanClass = getBeanClass(false);
      if (beanClass != null) {
        PsiMethod method = findMatchingFactoryMethod(name, beanClass);
        if (method != null) {
          return ReadOnlyGenericValue.getInstance(method);
        }
      }
    }
    return super.getFactoryMethod();
  }

  @Nullable
  private PsiMethod findMatchingFactoryMethod(String name, PsiClass beanClass) {
    PsiMethod result = null;
    PsiType returnType = null;
    int count = myInfo.constructorArgumentCount;
    for (PsiMethod method : beanClass.findMethodsByName(name, true)) {
      if (method.getParameterList().getParametersCount() == count && method.isStatic()) {
        if (returnType == null) {
          result = method;
          returnType = method.getReturnType();
        } else if (!returnType.equals(method.getReturnType())) {
          return null;
        }
      }
    }
    return result;
  }

  @Nullable
  @Override
  public String getBeanName() {
    return myInfo.beanName;
  }

  @Nonnull
  @Override
  public String[] getAliases() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  @RequiredReadAction
  public boolean isValid() {
    return mySourceTag.isValid();
  }

  @Nonnull
  @Override
  public XmlTag getXmlTag() {
    return mySourceTag;
  }

  @Override
  public PsiManager getPsiManager() {
    return mySourceTag.getManager();
  }

  @Nullable
  @Override
  public Module getModule() {
    return myModule;
  }

  @Nullable
  @Override
  public PsiElement getIdentifyingPsiElement() {
    return myFakePsi;
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public final PsiFile getContainingFile() {
    return mySourceTag.getContainingFile();
  }

  @Nullable
  @Override
  public String getClassName() {
    return myInfo.beanClassName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CustomNamespaceSpringBean that = (CustomNamespaceSpringBean)o;

    return Objects.equals(myInfo, that.myInfo)
      && Objects.equals(myWrapper, that.myWrapper);
  }

  @Override
  public int hashCode() {
    return 31 * Objects.hashCode(myInfo) + Objects.hashCode(myWrapper);
  }
}
