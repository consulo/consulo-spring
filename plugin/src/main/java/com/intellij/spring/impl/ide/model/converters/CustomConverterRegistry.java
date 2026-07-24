package com.intellij.spring.impl.ide.model.converters;

import consulo.util.lang.function.Condition;
import consulo.util.lang.Pair;
import consulo.xml.dom.Converter;
import consulo.xml.dom.GenericDomValue;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CustomConverterRegistry {
  private final Map<Class, Pair<Condition<GenericDomValue>, Converter>> myCustomConverters =
    new HashMap<Class, Pair<Condition<GenericDomValue>, Converter>>();

  public void registryConverter(Class aClass, Pair<Condition<GenericDomValue>, Converter> pair) {
    myCustomConverters.put(aClass, pair);
  }

  @Nullable
  public Converter getCustomConverter(Class aClass, GenericDomValue context) {
    Pair<Condition<GenericDomValue>, Converter> pair = myCustomConverters.get(aClass);
    if (pair != null && pair.first.value(context)) {
      return pair.second;
    }
    return null;
  }

}
