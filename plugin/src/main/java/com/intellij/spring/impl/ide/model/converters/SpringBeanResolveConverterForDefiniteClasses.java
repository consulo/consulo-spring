package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.xml.dom.ConvertContext;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class SpringBeanResolveConverterForDefiniteClasses extends SpringBeanResolveConverter {

  @Nullable
  protected abstract String[] getClassNames(ConvertContext context) ;

  @Nullable
  public List<PsiClassType> getRequiredClasses(ConvertContext context) {
    List<PsiClassType> required = new ArrayList<PsiClassType>();
    PsiManager psiManager = context.getPsiManager();
    String[] strings = getClassNames(context);

    if (strings == null || strings.length == 0) return null;

    for (String className : strings) {
      PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject())
                                             .findClass(className, GlobalSearchScope.allScope(psiManager.getProject()));

      if (psiClass != null) {
        required.add(JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
      }
    }


    return required;
  }
}
