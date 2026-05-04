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

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Dmitry Avdeev
 */
public class SpringElContextProvider implements ELContextProvider {

  private final XmlAttributeValue myHost;

  public SpringElContextProvider(XmlAttributeValue host) {
    myHost = host;
  }

  public Iterator<? extends PsiVariable> getTopLevelElVariables(@Nullable String nameHint) {
    SpringModel springModel = SpringManager.getInstance(myHost.getProject()).getSpringModelByFile((XmlFile)myHost.getContainingFile());
    assert springModel != null;
    if (nameHint == null) {
      ArrayList<JspImplicitVariable> vars = new ArrayList<JspImplicitVariable>();
      SpringBeansAsJsfVariableUtil.addVariables(vars, springModel);
      vars.add(new SystemPropertiesVariable(myHost));
      return vars.iterator();
    } else if (nameHint.equals(SystemPropertiesVariable.SYSTEM_PROPERTIES)){
      return Collections.singleton(new SystemPropertiesVariable(myHost)).iterator();
    } else {
      SpringBeanPointer bean = springModel.findBean(nameHint);
      if (bean != null) {
        JspImplicitVariableImpl variable = SpringBeansAsJsfVariableUtil.createVariable(bean, nameHint);
        return Collections.singleton(variable).iterator();
      }
    }
    return null;
  }

  public boolean acceptsGetMethodForLastReference(PsiMethod getter) {
    return true;
  }

  public boolean acceptsSetMethodForLastReference(PsiMethod setter) {
    return true;
  }

  public boolean acceptsNonPropertyMethodForLastReference(PsiMethod method) {
    return true;
  }
}
