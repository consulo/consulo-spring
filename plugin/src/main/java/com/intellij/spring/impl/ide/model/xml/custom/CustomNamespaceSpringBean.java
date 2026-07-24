/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.custom;

import com.intellij.jam.model.common.ReadOnlyGenericValue;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.CustomBeanInfo;
import com.intellij.spring.impl.ide.CustomBeanRegistry;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.model.AbstractDomSpringBean;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.util.collection.ArrayUtil;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.GenericValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
  public XmlAttribute getIdAttribute() {
    return myIdAttribute;
  }

  @Nullable
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
  public CustomBeanWrapper getWrapper() {
    return myWrapper;
  }

  @Nullable
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
      if (method.getParameterList().getParametersCount() == count && method.hasModifierProperty(PsiModifier.STATIC)) {
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
  public String getBeanName() {
    return myInfo.beanName;
  }

  @Nonnull
  public String[] getAliases() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public boolean isValid() {
    return mySourceTag.isValid();
  }

  @Nonnull
  public XmlTag getXmlTag() {
    return mySourceTag;
  }

  public PsiManager getPsiManager() {
    return mySourceTag.getManager();
  }

  @Nullable
  public Module getModule() {
    return myModule;
  }

  @Nullable
  public PsiElement getIdentifyingPsiElement() {
    return myFakePsi;
  }

  @Nonnull
  public final PsiFile getContainingFile() {
    return mySourceTag.getContainingFile();
  }

  @Nullable
  public String getClassName() {
    return myInfo.beanClassName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CustomNamespaceSpringBean that = (CustomNamespaceSpringBean)o;

    if (myInfo != null ? !myInfo.equals(that.myInfo) : that.myInfo != null) return false;
    if (myWrapper != null ? !myWrapper.equals(that.myWrapper) : that.myWrapper != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInfo != null ? myInfo.hashCode() : 0;
    result = 31 * result + (myWrapper != null ? myWrapper.hashCode() : 0);
    return result;
  }
}
