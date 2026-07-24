package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValue;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringValueImpl implements SpringValue {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    PsiType type = getType().getValue();
    return type == null ? Collections.<PsiType>emptyList() : Collections.singletonList(type);
  }
}
