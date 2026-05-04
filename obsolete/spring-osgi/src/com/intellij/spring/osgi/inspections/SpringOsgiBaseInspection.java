package com.intellij.spring.osgi.inspections;

import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.highlighting.SpringBeanInspectionBase;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.osgi.model.xml.BaseOsgiReference;
import com.intellij.spring.osgi.model.xml.Service;
import com.intellij.spring.osgi.model.xml.BaseReferenceCollection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;

public abstract class SpringOsgiBaseInspection extends SpringBeanInspectionBase {

  protected SpringModelVisitor createVisitor(final DomElementAnnotationHolder holder, final Beans beans, final SpringModel model) {
    return new SpringModelVisitor() {

      protected boolean visitBean(CommonSpringBean bean) {
        if (bean instanceof Service) {
          checkOsgiService((Service)bean, beans, holder, model);
        } else if (bean instanceof BaseOsgiReference) {
          checkOsgiReference((BaseOsgiReference)bean, beans, holder, model);
        }
        
        if (bean instanceof BaseReferenceCollection) {
          checkOsgiReferenceCollection((BaseReferenceCollection)bean, beans, holder, model);
        }

        return true;
      }
    };
  }

  protected void checkOsgiReferenceCollection(BaseReferenceCollection baseReferenceCollection, Beans beans, DomElementAnnotationHolder holder, SpringModel model) {}
  protected void checkOsgiService(Service service, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {}
  protected void checkOsgiReference(BaseOsgiReference reference, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel springModel) {}
}

