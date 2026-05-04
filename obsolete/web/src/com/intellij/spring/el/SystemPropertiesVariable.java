/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.el;

import com.intellij.lang.properties.psi.PropertiesElementFactory;
import com.intellij.lang.properties.psi.Property;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.impl.source.jsp.el.impl.ELElementProcessor;
import com.intellij.psi.impl.source.jsp.el.impl.JspImplicitVariableWithCustomResolve;
import com.intellij.psi.jsp.el.ELExpression;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SystemPropertiesVariable extends JspImplicitVariableImpl implements JspImplicitVariableWithCustomResolve {

  @NonNls public static final String SYSTEM_PROPERTIES = "systemProperties";

  public SystemPropertiesVariable(PsiElement element) {
    super(element, SYSTEM_PROPERTIES, getType(element), element, NESTED_RANGE);
  }

  private static PsiType getType(PsiElement element) {
    return JavaPsiFacade.getInstance(element.getProject()).getElementFactory().createTypeByFQClassName(CommonClassNames.JAVA_UTIL_MAP, element.getResolveScope());
  }

  public boolean process(ELExpression element, ELElementProcessor processor) {    
    for (Property property: PropertiesElementFactory.getSystemProperties(element.getProject()).getProperties()) {
      if (!processor.processProperty(property)) {
        return false;
      }
    }
    return true;
  }
}
