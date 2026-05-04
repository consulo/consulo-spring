package com.intellij.spring.security.model.xml.converters;

import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.WrappingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Sergey.Vasiliev
 */
public class InterceptUrlAccessWrappingConverter extends WrappingConverter {
  public static Converter ROLES_CONVERTER = new InterceptUrlAccessRolesConverter();
  public static SecurityExpressionRootMethodsConverter EXPRESSION_ROOT_METHODS_CONVERTER = new SecurityExpressionRootMethodsConverter();

  @Nullable
  public Converter getConverter(@NotNull final GenericDomValue element) {
    if (EXPRESSION_ROOT_METHODS_CONVERTER.getExpressionRootMethod(element) != null) {
      return EXPRESSION_ROOT_METHODS_CONVERTER;
    }

    return ROLES_CONVERTER;
  }
}