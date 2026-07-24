/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringEntry;
import com.intellij.spring.impl.ide.model.xml.beans.SpringMap;
import com.intellij.spring.impl.ide.model.xml.beans.TypeHolder;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericAttributeValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringEntryImpl extends SpringInjectionImpl implements SpringEntry {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    SpringMap map = (SpringMap)getParent();
    List<PsiType> psiTypes = TypedCollectionImpl.getRequiredTypes(map);
    if (!psiTypes.isEmpty()) {
      return psiTypes;
    }
    assert map != null;
    return Collections.singletonList(getRequiredTypeFromGenerics(map, 1));
  }

  @Nullable
  public PsiClass getRequiredKeyClass() {
    SpringMap map = (SpringMap)getParent();
    assert map != null;
    PsiClass psiClass = map.getKeyType().getValue();
    if (psiClass != null) {
      return psiClass;
    }
    PsiType keyType = getRequiredTypeFromGenerics(map, 0);
    return keyType instanceof PsiClassType ? ((PsiClassType)keyType).resolve() : null;
  }

  @Nullable
  public PsiType getRequiredKeyType() {
    SpringMap map = (SpringMap)getParent();
    assert map != null;
    PsiClass psiClass = map.getKeyType().getValue();
    if (psiClass != null) {
      return JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass);
    }
    return getRequiredTypeFromGenerics(map, 0);
  }

  @Nullable
  private static PsiType getRequiredTypeFromGenerics(@Nonnull SpringMap map, int index) {
    DomElement parent = map.getParent();
    if (parent instanceof TypeHolder) {
      List<? extends PsiType> types = ((TypeHolder)parent).getRequiredTypes();
      for (PsiType type : types) {
        if (type instanceof PsiClassType) {
          PsiClassType.ClassResolveResult resolveResult = ((PsiClassType)type).resolveGenerics();
          PsiClass psiClass = resolveResult.getElement();
          PsiSubstitutor substitutor = resolveResult.getSubstitutor();
          if (psiClass != null && substitutor != null) {
            PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
            if (typeParameters.length == 2) {
              return substitutor.substitute(typeParameters[index]);
            }
          }
        }
      }
    }
    return null;
  }

  @Nonnull
  public GenericAttributeValue<SpringBeanPointer> getRefAttr() {
    return getValueRef();        
  }

}