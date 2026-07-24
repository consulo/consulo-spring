package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.TypeHolder;
import com.intellij.spring.impl.ide.model.xml.beans.TypedCollection;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public abstract class TypedCollectionImpl implements TypedCollection, TypeHolder {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    return getRequiredTypes(this);
  }

  @Nonnull
  public static List<PsiType> getRequiredTypes(TypedCollection collection) {
    PsiClass psiClass = collection.getValueType().getValue();
    return psiClass == null ? Collections.<PsiType>emptyList() : Collections.<PsiType>singletonList(
      JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
  }
}
