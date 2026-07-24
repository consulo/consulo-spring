package com.intellij.spring.osgi.model.converters;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.osgi.model.xml.Service;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

// method signature must be:
// public void anyMethodName(ServiceType serviceInstance, Map serviceProperties);
// public void anyMethodName(ServiceType serviceInstance, Dictionary serviceProperties);

// where ServiceType can be any type compatible with the exported service interface of the service
public class RegistrationListenerMethodConverter extends BasicListenerMethodConverter {

  protected boolean checkParameterList(PsiMethod method, ConvertContext context) {
    Project project = method.getProject();
    PsiParameter[] parameters = method.getParameterList().getParameters();
    if (parameters.length == 2) {
      PsiType type2 = parameters[1].getType();

      return checkServiceTypeParameter(context, parameters[0].getType()) &&
             (checkType(type2, CommonClassNames.JAVA_UTIL_MAP, project) ||
              checkType(type2, CommonClassNames.JAVA_UTIL_DICTIONARY, project));

    }
    return false;
  }

  private static boolean checkServiceTypeParameter(ConvertContext context, PsiType type) {
    Service service = context.getInvocationElement().getParentOfType(Service.class, false);

    if (service != null && type != null) {
      PsiClass beanClass = getServiceBeanClass(service);
      if (beanClass != null) {
        return type.isAssignableFrom(JavaPsiFacade.getInstance(context.getModule().getProject()).getElementFactory().createType(beanClass));
      }

    }
    return true;
  }

  @Nullable
  private static PsiClass getServiceBeanClass(Service service) {
    SpringBeanPointer value = service.getRef().getValue();
    if (value != null) {
      return value.getBeanClass();
    }
    return service.getBean().getBeanClass();
  }
}
