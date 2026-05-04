package com.intellij.spring.osgi.inspections;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.osgi.SpringOsgiBundle;
import com.intellij.spring.osgi.constants.SpringOsgiConstants;
import com.intellij.spring.osgi.model.converters.ReferenceListenerMethodConverter;
import com.intellij.spring.osgi.model.xml.BaseOsgiReference;
import com.intellij.spring.osgi.model.xml.Listener;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringOsgiListenerInspection extends SpringOsgiBaseInspection {

  @Override
  protected void checkOsgiReference(BaseOsgiReference reference, Beans beans, DomElementAnnotationHolder holder, SpringModel springModel) {
    for (Listener listener : reference.getListeners()) {
      checkBindMethodsSignature(listener, holder);
      checkListenerClass(listener, holder);
    }
  }

  private void checkListenerClass(Listener listener, DomElementAnnotationHolder holder) {
    SpringBeanPointer springBeanPointer = listener.getRef().getValue();
    if (springBeanPointer != null) {
      checkListenerClass(holder, springBeanPointer.getBeanClass(), listener.getRef());
    }

    SpringBean bean = listener.getBean();
    if (bean != null) {
      checkListenerClass(holder, bean.getBeanClass(), bean);
    }
  }

  private void checkListenerClass(DomElementAnnotationHolder holder, @Nullable PsiClass psiClass, DomElement element) {
    if (psiClass == null) return;
    if (!InheritanceUtil.isInheritor(psiClass, SpringOsgiConstants.OSGI_SERVICE_LIFECYCLE_LISTENER_CLASSNAME)) {
      holder.createProblem(element, SpringOsgiBundle.message("model.inspection.listener.class.extends", SpringOsgiConstants.OSGI_SERVICE_LIFECYCLE_LISTENER_CLASSNAME));
    }
  }

  private void checkBindMethodsSignature(Listener listener, DomElementAnnotationHolder holder) {
    checkMethodSignature(listener.getBindMethod(), holder);
    checkMethodSignature(listener.getUnbindMethod(), holder);
  }

  //public void anyMethodName(ServiceType service, Dictionary properties);
  //public void anyMethodName(ServiceType service, Map properties);
  //public void anyMethodName(ServiceReference ref);
  private void checkMethodSignature(GenericAttributeValue<PsiMethod> method, DomElementAnnotationHolder holder) {
    PsiMethod psiMethod = method.getValue();
    if (psiMethod != null) {
      final PsiType returnType = psiMethod.getReturnType();
      if (!PsiType.VOID.equals(returnType)) {
        holder.createProblem(method, SpringOsgiBundle.message("model.inspection.listener.common.method.return.type"));
      }
      if (!psiMethod.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
        holder.createProblem(method, SpringOsgiBundle.message("model.inspection.listener.common.method.public"));
      }

      if (!ReferenceListenerMethodConverter.checkProperParameters(psiMethod) ) {
        holder.createProblem(method, SpringOsgiBundle.message("model.inspection.listener.common.method.parameters"));
      }
    }
  }

  @NotNull
  public String getDisplayName() {
    return SpringOsgiBundle.message("model.inspection.listener.common");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringOsgiListenerInspection";
  }
}