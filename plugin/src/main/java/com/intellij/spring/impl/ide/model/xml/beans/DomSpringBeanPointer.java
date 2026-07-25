/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.application.progress.ProgressManager;
import consulo.application.util.NotNullLazyValue;
import consulo.application.util.NullableLazyValue;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.util.lang.Comparing;
import consulo.util.lang.ref.PatchedWeakReference;
import consulo.xml.dom.DomAnchor;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.util.xml.impl.DomAnchorImpl;
import jakarta.annotation.Nonnull;

import java.lang.ref.WeakReference;

/**
 * @author peter
 */
public class DomSpringBeanPointer extends SpringBaseBeanPointer {
  @Nonnull
  private final DomAnchor<DomSpringBean> myPointer;
  private final NotNullLazyValue<Boolean> myAbstract = new NotNullLazyValue<>() {
    @Nonnull
    @Override
    protected Boolean compute() {
      if (getSpringBean() instanceof SpringBean bean1) {
        Boolean value = bean1.getAbstract().getValue();
        return value != null && value;
      }
      return false;
    }
  };
  private final NullableLazyValue<SpringBeanPointer> myParent = new NullableLazyValue<>() {
    @Override
    protected SpringBeanPointer compute() {
      return getSpringBean() instanceof SpringBean bean ? bean.getParentBean().getValue() : null;
    }
  };
  private final NullableLazyValue<PsiClass> myBeanClass = new NullableLazyValue<>() {
    @Override
    protected PsiClass compute() {
      return getSpringBean().getBeanClass();
    }
  };

  private WeakReference<DomSpringBean> myCachedValue;

  private DomSpringBeanPointer(@Nonnull DomSpringBean springBean) {
    super(springBean.getBeanName());
    ProgressManager.getInstance().checkCanceled();
    myCachedValue = new PatchedWeakReference<>(springBean);
    XmlTag tag = springBean.getXmlTag();
    assert tag != null;
    myPointer = DomAnchorImpl.createAnchor(springBean);
  }

  @Nonnull
  @Override
  public DomSpringBean getSpringBean() {
    DomSpringBean bean = myCachedValue.get();
    if (bean != null) return bean;

    bean = myPointer.retrieveDomElement();
    assert bean != null : "No bean at pointer";
    myCachedValue = new PatchedWeakReference<>(bean);
    return bean;
  }

  @Override
  public boolean isValid() {
    DomSpringBean bean = myCachedValue.get();
    if (bean != null) return bean.isValid();

    bean = myPointer.retrieveDomElement();
    if (bean != null && bean.isValid()) {
      myCachedValue = new PatchedWeakReference<>(bean);
      return true;
    }
    return false;
  }

  @Override
  public PsiManager getPsiManager() {
    return PsiManager.getInstance(getContainingFile().getProject());
  }

  public static DomSpringBeanPointer createDomSpringBeanPointer(@Nonnull DomSpringBean bean) {
    return new DomSpringBeanPointer(bean);
  }

  @Override
  public synchronized boolean isAbstract() {
    return myAbstract.getValue();
  }

  @Override
  public synchronized SpringBeanPointer getParentPointer() {
    return myParent.getValue();
  }

  @Override
  public PsiElement getPsiElement() {
    return getSpringBean().getXmlElement();
  }

  @Override
  public SpringBeanPointer derive(@Nonnull String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(this, name);
  }

  @Override
  public synchronized PsiClass getBeanClass() {
    return myBeanClass.getValue();
  }

  @Override
  public PsiFile getContainingFile() {
    return myPointer.getContainingFile();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    return o instanceof DomSpringBeanPointer that
      && myPointer.equals(that.myPointer);
  }

  @Override
  public int hashCode() {
    return myPointer.hashCode();
  }
}
