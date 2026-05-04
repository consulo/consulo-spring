package com.intellij.spring.el;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomNamedTarget;
import com.intellij.pom.references.PomService;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.jam.javaConfig.JavaSpringJavaBean;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SpringBeansAsJsfVariableUtil {
  private SpringBeansAsJsfVariableUtil() {
  }

  public static void addVariables(final List<JspImplicitVariable> resultVars, final Module module) {
    final SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    if (springModel == null) return;

    addVariables(resultVars, springModel);
  }

  public static void addVariables(final List<JspImplicitVariable> resultVars, final SpringModel springModel) {
    final Collection<? extends SpringBeanPointer> list = springModel.getAllCommonBeans(true);

    for (SpringBeanPointer pointer : list) {
      final PsiFile file = pointer.getContainingFile();
      if (file != null) {
        final PsiClassType type = getBeanType(pointer);
        //todo peter: beautify
        CommonSpringBean bean = pointer.getSpringBean();
        if (bean instanceof JavaSpringJavaBean) {
          for (PomNamedTarget target : ((JavaSpringJavaBean)bean).getPomTargets()) {
            PsiElement psiElement = PomService.convertToPsi((PsiTarget)target);
            resultVars.add(createVariable(pointer, file, target.getName(), psiElement, type));
          }
          continue;
        }

        final String beanName = pointer.getName();
        if (beanName != null && !StringUtil.isEmptyOrSpaces(beanName)) {
          final PsiElement element = pointer.getPsiElement();
          if (element != null) {
            final Set<String> beanNames = springModel.getAllBeanNames(beanName);
            for (String aliasName : beanNames) {
              if (!StringUtil.isEmptyOrSpaces(aliasName)) {
                resultVars.add(createVariable(pointer, file, aliasName, element, type));
              }
            }
          }
        }
      }
    }
  }

  private static JspImplicitVariableImpl createVariable(final SpringBeanPointer commonSpringBean,
                                                        final PsiFile file,
                                                        final String beanName,
                                                        final PsiElement element,
                                                        final PsiClassType type) {
    return new JspImplicitVariableImpl(file, beanName, type, element, JspImplicitVariableImpl.NESTED_RANGE) {

      @Nullable
      public Icon getIcon(final boolean open) {
        return commonSpringBean.getBeanIcon();
      }
    };
  }

  public static JspImplicitVariableImpl createVariable(final SpringBeanPointer commonSpringBean, String name) {
    PsiClassType type = getBeanType(commonSpringBean);
    return createVariable(commonSpringBean, commonSpringBean.getContainingFile(), name, commonSpringBean.getPsiElement(), type);
  }

  private static PsiClassType getBeanType(SpringBeanPointer commonSpringBean) {
    PsiClass[] classes = commonSpringBean.getEffectiveBeanType();
    PsiClassType type;
    if (classes.length == 0) {
      type = PsiClassType.getJavaLangObject(commonSpringBean.getPsiManager(),
                                            GlobalSearchScope.allScope(commonSpringBean.getPsiManager().getProject()));
    } else {
      PsiClass psiClass = classes[0];
      type = JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass);
    }
    return type;
  }

}
