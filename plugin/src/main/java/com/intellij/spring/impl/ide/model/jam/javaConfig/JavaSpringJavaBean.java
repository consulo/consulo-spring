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

package com.intellij.spring.impl.ide.model.jam.javaConfig;

import com.intellij.jam.JamPomTarget;
import com.intellij.jam.JamSimpleReferenceConverter;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import consulo.language.pom.PomNamedTarget;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class JavaSpringJavaBean extends SpringJavaBean {

  private static final JamStringAttributeMeta.Collection<String> NAME_VALUE_META = JamAttributeMeta.collectionString("name");

  private static final JamStringAttributeMeta.Single<PsiMethod> INIT_METHOD_META =
    JamAttributeMeta.singleString("initMethod", new PsiMethodReferenceConverter());

  private static final JamStringAttributeMeta.Single<PsiMethod> DESTROY_METHOD_META =
    JamAttributeMeta.singleString("destroyMethod", new PsiMethodReferenceConverter());


  public static JamAnnotationMeta META =
    new JamAnnotationMeta(SpringAnnotationsConstants.SPRING_BEAN_ANNOTATION).addAttribute(NAME_VALUE_META)
                                                                            .addAttribute(INIT_METHOD_META).addAttribute(DESTROY_METHOD_META);


  public PsiAnnotation getPsiAnnotation() {
    return META.getAnnotation(getPsiElement());
  }

  @Override
  public String getBeanName() {
    List<JamStringAttributeElement<String>> elements = META.getAttribute(getPsiElement(), NAME_VALUE_META);
    if (elements.size() == 1) {
      return elements.get(0).getValue();
    }

    return super.getBeanName();
  }

  @Nonnull
  public String[] getAliases() {
   // @Bean "name" attribute: The name of this bean, or if plural, aliases for this bean.
    List<JamStringAttributeElement<String>> elements = META.getAttribute(getPsiElement(), NAME_VALUE_META);
    
    if (elements.size() < 2) return new String[0];

    List<String> aliases = getStringNames(elements);

    return aliases.toArray(new String[aliases.size()]);
  }

  public List<PomNamedTarget> getPomTargets() {
    List<PomNamedTarget> pomTargets = new ArrayList<PomNamedTarget>();

    List<JamStringAttributeElement<String>> elements = META.getAttribute(getPsiElement(), NAME_VALUE_META);
    if (!elements.isEmpty()) {
      for (JamStringAttributeElement<String> attributeElement : elements) {
        pomTargets.add(new JamPomTarget(this, attributeElement));
      }
    } else {
      pomTargets.add(getPsiElement());
    }

    return pomTargets;
  }

  private static class PsiMethodReferenceConverter extends JamSimpleReferenceConverter<PsiMethod> {
    public PsiMethod fromString(@Nullable String s, JamStringAttributeElement<PsiMethod> context) {
      for (PsiMethod psiMethod : getAppropriateMethods(context)) {
        if (psiMethod.getName().equals(s)) {
          return psiMethod;
        }
      }
      return null;
    }

    private List<PsiMethod> getAppropriateMethods(JamStringAttributeElement<PsiMethod> context) {
      List<PsiMethod> methods = new ArrayList<PsiMethod>();
      PsiMethod method = PsiTreeUtil.getParentOfType(context.getPsiElement(), PsiMethod.class);

      if (method != null) {
        PsiType type = method.getReturnType();
        if (type instanceof PsiClassType) {
          PsiClass psiClass = ((PsiClassType)type).resolve();
          for (PsiMethod psiMethod : psiClass.getAllMethods()) {
            if (!psiMethod.isConstructor() && psiMethod.getParameterList().getParametersCount() == 0) {
              methods.add(psiMethod);
            }
          }
        }
      }
      return methods;
    }

    @Override
    public Collection<PsiMethod> getVariants(JamStringAttributeElement<PsiMethod> context) {
      List<PsiMethod> methods = new ArrayList<PsiMethod>();
      for (PsiMethod method : getAppropriateMethods(context)) {
        if (!CommonClassNames.JAVA_LANG_OBJECT.equals(method.getContainingClass().getQualifiedName())) {
          methods.add(method);
        }
      }
      return methods;
    }
  }
}