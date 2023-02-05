package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.impl.util.xml.PsiClassConverter;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.util.xml.ConvertContext;

/**
 * @author Dmitry Avdeev
 */
public class SpringQualifierTypeConverter extends PsiClassConverter {

  public PsiClass fromString(final String s, final ConvertContext context) {
    if (s == null) {
      final Project project = context.getPsiManager().getProject();
      return JavaPsiFacade.getInstance(project).findClass("org.springframework.beans.factory.annotation.Qualifier",
                                                          GlobalSearchScope.allScope(project));
    }
    return super.fromString(s, context);
  }
}
