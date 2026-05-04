package com.intellij.spring.security.model;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ModelVersion {
  public abstract SpringSecurityVersion value() default SpringSecurityVersion.SpringSecurity_3_0;

}