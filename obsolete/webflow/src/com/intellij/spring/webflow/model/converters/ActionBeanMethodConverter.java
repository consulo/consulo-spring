package com.intellij.spring.webflow.model.converters;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: plt
 */
public class ActionBeanMethodConverter extends ResolvingConverter<PsiMethod> {

  @NotNull
  public Collection<? extends PsiMethod> getVariants(ConvertContext context) {
    WebflowNamedAction action = context.getInvocationElement().getParentOfType(WebflowNamedAction.class, false);

    Set<PsiMethod> variants = new HashSet<PsiMethod>();
    List<String> addedMethodNames = new ArrayList<String>();
    for (PsiMethod method : getAllMethods(action)) {
      if (addedMethodNames.contains(method.getName())) continue;
      if (!method.hasModifierProperty(PsiModifier.PUBLIC) || method.isConstructor() || CommonClassNames.JAVA_LANG_OBJECT.equals(method.getContainingClass().getQualifiedName())) continue;
      
      boolean isAction = WebflowUtil.isAction(context);
      if ((isAction && isActionBeanMethod(method)) || ! isAction) {
        variants.add(method); // IDEADEV-26886
        addedMethodNames.add(method.getName());
      }

    }
    return variants;
  }

  private static boolean isActionBeanMethod(PsiMethod method) {
    PsiType returnType= method.getReturnType();
    return method.getParameterList().getParameters().length == 1 &&
           WebflowConstants.ACTION_BEAN_METHOD_PARAMETER_CLASSNAME.equals(method.getParameterList().getParameters()[0].getType().getCanonicalText()) &&
           returnType != null && WebflowConstants.ACTION_BEAN_METHOD_RETURN_TYPE_CLASSNAME.equals(returnType.getCanonicalText())  ;
  }


  private static Collection<? extends PsiMethod> getAllMethods(WebflowNamedAction action) {
    Set<PsiMethod> methods = new HashSet<PsiMethod>();
    if (action != null) {
      SpringBeanPointer bean = action.getBean().getValue();
      if (bean != null && bean.getBeanClass() != null) {
        methods.addAll(Arrays.asList(bean.getBeanClass().getAllMethods()));
      }
    }
    return methods;
  }

  public PsiMethod fromString(@Nullable @NonNls String s, ConvertContext context) {
    PsiMethod resolvedMethod = null;
    if (s != null) {
      WebflowNamedAction action = context.getInvocationElement().getParentOfType(WebflowNamedAction.class, false);

      for (PsiMethod method : getAllMethods(action)) {
        if (s.equals(method.getName())) {   // IDEADEV-26973
          if (resolvedMethod == null || method.getContainingClass().isInheritor(resolvedMethod.getContainingClass(), true)) {
            resolvedMethod = method;
          }
        }
      }
    }
    return resolvedMethod;
  }

  public String toString(@Nullable PsiMethod psiMethod, ConvertContext context) {
    return psiMethod != null ? psiMethod.getName() : null;
  }
}
