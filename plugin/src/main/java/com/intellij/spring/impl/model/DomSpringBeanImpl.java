package com.intellij.spring.impl.model;

import jakarta.annotation.Nonnull;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.xml.dom.DomUtil;
import consulo.language.psi.PsiFile;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nullable;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class DomSpringBeanImpl extends AbstractDomSpringBean implements DomSpringBean {
  @Nullable
  @Override
  public String getBeanName() {
    return getId().getStringValue();
  }

  @Override
  public void setName(@Nonnull String newName) {
    if (getBeanName() != null) {
      getId().setStringValue(newName);
    }
  }

  @Nullable
  @Override
  public PsiFile getContainingFile() {
    return DomUtil.getFile(this);
  }

  @Nullable
  @Override
  public PsiElement getIdentifyingPsiElement() {
    return getXmlElement();
  }

  @Nonnull
  @Override
  public String[] getAliases() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  public PsiManager getPsiManager() {
    return PsiManager.getInstance(getManager().getProject());
  }
}
