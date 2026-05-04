package com.intellij.spring.webflow.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SetOwner {
  @NotNull
  List<Set> getSets();

  Set addSet();
}
