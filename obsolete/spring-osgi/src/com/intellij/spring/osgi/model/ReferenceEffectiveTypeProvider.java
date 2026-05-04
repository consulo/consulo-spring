package com.intellij.spring.osgi.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.SpringBeanEffectiveTypeProvider;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.spring.osgi.model.xml.BaseOsgiReference;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ReferenceEffectiveTypeProvider extends SpringBeanEffectiveTypeProvider {
  @NonNls private final String[] UnboxingReferencesClassNames = new String[]{"org.osgi.framework.ServiceReference"};

  public void processEffectiveTypes(@NotNull final CommonSpringBean bean, final Collection<PsiClass> result) {
    if (!(bean instanceof BaseOsgiReference)) return;

    result.clear();
    final Project project = bean.getPsiManager().getProject();

    final BaseOsgiReference reference = (BaseOsgiReference)bean;
    final PsiClass psiClass = reference.getInterface().getValue();
    ContainerUtil.addIfNotNull(psiClass, result);
    for (SpringValue value : reference.getInterfaces().getValues()) {
      addClass(result, project, value.getStringValue());
    }
    for (String className : UnboxingReferencesClassNames) {
      // 6.2.1.9 http://static.springframework.org/osgi/docs/1.2.0-m1/reference/html/service-registry.html
      addClass(result, project, className);
    }
  }

  private static void addClass(final Collection<PsiClass> classes, final Project project, final String className) {
    if (!StringUtil.isEmptyOrSpaces(className)) {
      final PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
      ContainerUtil.addIfNotNull(aClass, classes);
    }
  }
}
