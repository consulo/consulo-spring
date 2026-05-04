package com.intellij.spring.security.constants;

import org.jetbrains.annotations.NonNls;

public interface SpringSecurityConstants {
  @NonNls String SECURITY_NAMESPACE_KEY = "Spring Security namespace key";
  @NonNls String SECURITY_NAMESPACE = "http://www.springframework.org/schema/security";
  @NonNls String SECURITY_TAGS_NAMESPACE = "http://www.springframework.org/security/tags";

  @NonNls String SECURITY_SCHEMA_3_0 = "http://www.springframework.org/schema/security/spring-security-3.0.xsd";
  @NonNls String SECURITY_SCHEMA_2_0 = "http://www.springframework.org/schema/security/spring-security-2.0.xsd";
  @NonNls String SECURITY_SCHEMA_2_0_1 = "http://www.springframework.org/schema/security/spring-security-2.0.1.xsd";
  @NonNls String SECURITY_SCHEMA_2_0_2 = "http://www.springframework.org/schema/security/spring-security-2.0.2.xsd";
  @NonNls String SECURITY_SCHEMA_2_0_4 = "http://www.springframework.org/schema/security/spring-security-2.0.4.xsd";


  @NonNls String SECURED_ANNOTATION = "org.springframework.security.access.annotation.Secured";
}
