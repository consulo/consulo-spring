/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.java.impl.util.xml.DomJavaUtil;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.model.converters.SpringBeanResolveConverter;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.DomElement;

import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class TypedBeanResolveConverter extends SpringBeanResolveConverter {

  @Nullable
  public List<PsiClassType> getRequiredClasses(ConvertContext context) {
    DomElement element = context.getInvocationElement();
    RequiredBeanType type = element.getAnnotation(RequiredBeanType.class);
    assert type != null;
    PsiClass aClass = DomJavaUtil.findClass(type.value(), context.getFile(), context.getModule(), null);
    return aClass == null ? null : Arrays.asList(JavaPsiFacade.getInstance(context.getFile().getProject()).getElementFactory().createType(aClass));
  }
}
