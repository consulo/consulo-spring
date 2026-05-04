package com.intellij.spring.osgi.model.converters;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.model.converters.SpringBeanResolveConverterForDefiniteClasses;
import com.intellij.spring.model.xml.beans.SpringValue;
import com.intellij.spring.osgi.model.xml.Service;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;

import java.util.ArrayList;
import java.util.List;

public class ServiceBeanRefConverter extends SpringBeanResolveConverterForDefiniteClasses {

  protected String[] getClassNames(final ConvertContext context) {
    List<String> classes = new ArrayList<String>();
    final DomElement element = context.getInvocationElement();
    final Service service = element.getParentOfType(Service.class, false);
    if (service != null) {
      final PsiClass psiClass = service.getInterface().getValue();
      if (psiClass != null) {
        classes.add(psiClass.getQualifiedName());
      }

      final Module module = context.getModule();
      if (module != null) {
        for (SpringValue value : service.getInterfaces().getValues()) {
          final String stringValue = value.getStringValue();
          if (!StringUtil.isEmptyOrSpaces(stringValue)) {

            final PsiClass aClass =
                JavaPsiFacade.getInstance(module.getProject()).findClass(stringValue, GlobalSearchScope.allScope(module.getProject()));
            if (aClass != null) {
              classes.add(aClass.getQualifiedName());
            }
          }
        }
      }
    }

    return classes.isEmpty() ? new String[] {"java.lang.Object"}: classes.toArray(new String[classes.size()]) ;
  }
}
