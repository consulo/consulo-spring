package com.intellij.spring.security.inspections;

import com.intellij.spring.security.SpringSecurityBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class SpringSecurityElementsInconsistencyInspection extends SpringSecurityBaseInspection {

  @NotNull
  public String getDisplayName() {
    return SpringSecurityBundle.message("model.inspection.service.inconsistency");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringSecurityElementsInconsistencyInspection";
  }
}
