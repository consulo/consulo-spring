// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans
package com.intellij.spring.impl.ide.model.xml.beans;

import consulo.xml.dom.NamedEnum;
import jakarta.annotation.Nullable;

/**
 * http://www.springframework.org/schema/beans:autowireAttrType enumeration.
 */
public enum Autowire implements NamedEnum {
  AUTODETECT ("autodetect"),
  BY_NAME ("byName"),
  BY_TYPE ("byType"),
  CONSTRUCTOR ("constructor"),
  DEFAULT ("default"),
  NO ("no");

  private final String value;

  private Autowire(String value) { this.value = value; }

  @Override
  public String getValue() { return value; }

  public boolean isAutowired() { return !equals(DEFAULT) && !equals(NO); }

  public static Autowire fromDefault(@Nullable DefaultAutowire defaultAutowire) {
    if (defaultAutowire == null) {
      return DEFAULT;
    }
    switch (defaultAutowire) {
      case AUTODETECT:
        return AUTODETECT;
      case BY_NAME:
        return BY_NAME;
      case BY_TYPE:
        return BY_TYPE;
      case CONSTRUCTOR:
        return CONSTRUCTOR;
      case NO:
        return NO;
      default:
        return DEFAULT;
    }
  }
}
