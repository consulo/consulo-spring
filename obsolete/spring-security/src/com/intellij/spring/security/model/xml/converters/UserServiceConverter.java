package com.intellij.spring.security.model.xml.converters;

import com.intellij.spring.model.converters.SpringBeanResolveConverterForDefiniteClasses;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.ConvertContext;

public class UserServiceConverter extends SpringBeanResolveConverterForDefiniteClasses {
  protected String[] getClassNames(final ConvertContext context) {
    return new String[] {SpringSecurityClassesConstants.USER_DETAILS_SERVICE};
  }
}