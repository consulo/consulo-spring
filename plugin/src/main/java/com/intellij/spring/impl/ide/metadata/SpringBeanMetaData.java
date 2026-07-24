package com.intellij.spring.impl.ide.metadata;

import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.SpringBundle;
import consulo.xml.util.xml.DomMetaData;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.DomUtil;
import consulo.language.util.IncorrectOperationException;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * User: Sergey.Vasiliev
*/
public class SpringBeanMetaData extends DomMetaData<DomSpringBean> {

  @Nullable
  protected GenericDomValue getNameElement(DomSpringBean element) {
    GenericAttributeValue<String> id = element.getId();
    if (DomUtil.hasXml(id)) {
      return id;
    }
    if (element instanceof SpringBean) {
      GenericAttributeValue<List<String>> name = ((SpringBean)element).getName();
      if (DomUtil.hasXml(name)) {
        return name;
      }
    }
    return null;
  }

  public void setName(String name) throws IncorrectOperationException
  {
    getElement().setName(name);
  }

  public String getTypeName() {
    return SpringBundle.message("spring.bean");
  }
}
