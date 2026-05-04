package com.intellij.spring.osgi.model.xml;

import com.intellij.psi.PsiMethod;
import com.intellij.spring.osgi.model.converters.ReferenceListenerMethodConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface Listener extends BasicListener {

  @NotNull
  @Convert(ReferenceListenerMethodConverter.class)
  GenericAttributeValue<PsiMethod> getBindMethod();

  @NotNull
  @Convert(ReferenceListenerMethodConverter.class)
  GenericAttributeValue<PsiMethod> getUnbindMethod();
}
