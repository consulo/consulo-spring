package com.intellij.spring.security.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author Serega.Vasiliev
 */
public enum SpringSecurityVersion {
    SpringSecurity_3_0("3.0"),
    SpringSecurity_2_0("2.0");

    private final String myName;

    SpringSecurityVersion(String name) {
      myName = name;
    }

    @Override
    public String toString() {
      return myName;
    }

    @NotNull
    public String getVersion() {
      return myName;
    }
  }
