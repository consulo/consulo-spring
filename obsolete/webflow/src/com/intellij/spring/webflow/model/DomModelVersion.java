package com.intellij.spring.webflow.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

public interface DomModelVersion {
  DomModelVersion UNDEFINED = new DomModelVersion() {
    @NonNls
    @NotNull
    public String getVersion() {
      return "undefined";
    }
  };

  @NotNull
  String getVersion();
}
