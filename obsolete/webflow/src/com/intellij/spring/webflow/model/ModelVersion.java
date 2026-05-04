package com.intellij.spring.webflow.model;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ModelVersion {
  WebflowVersion value() default WebflowVersion.Webflow_1_0;
}
