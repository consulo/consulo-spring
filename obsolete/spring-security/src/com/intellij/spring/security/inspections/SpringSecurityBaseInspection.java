package com.intellij.spring.security.inspections;

import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.highlighting.SpringBeanInspectionBase;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;

public abstract class SpringSecurityBaseInspection extends SpringBeanInspectionBase {

  protected SpringModelVisitor createVisitor(final DomElementAnnotationHolder holder, final Beans beans, final SpringModel model) {
    return new SpringModelVisitor() {

      protected boolean visitBean(CommonSpringBean bean) {

        return true;
      }
    };
  }
}

