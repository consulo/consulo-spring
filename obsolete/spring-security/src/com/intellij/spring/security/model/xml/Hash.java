package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.NamedEnum;

/**
 * http://www.springframework.org/schema/security:hashAttrType enumeration.
 */
public enum Hash implements NamedEnum {
  MD4("md4"),
  MD5("md5"),
  PLAINTEXT("plaintext"),
  SHA("sha"),
  SHA_256("sha-256"),
  Hash_SHA("{sha}"),
  Hash_SSHA("{ssha}");

  private final String value;

  private Hash(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
