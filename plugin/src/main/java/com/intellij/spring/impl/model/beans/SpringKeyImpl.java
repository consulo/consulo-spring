/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.SpringEntry;
import com.intellij.spring.impl.ide.model.xml.beans.SpringKey;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringKeyImpl extends SpringValueHolderImpl implements SpringKey {
  
  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    SpringEntry entry = (SpringEntry)getParent();
    assert entry != null;
    PsiType type = entry.getRequiredKeyType();
    return type == null ? Collections.<PsiType>emptyList() : Collections.singletonList(type);
  }
}
