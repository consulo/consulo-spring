// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.impl.ide.model.xml.beans;

import consulo.xml.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * http://www.springframework.org/schema/beans:default-autowireAttrType enumeration.
 */
public enum DefaultAutowire implements NamedEnum {

    AUTODETECT ("autodetect"),
    BY_NAME ("byName"),
    BY_TYPE ("byType"),
    CONSTRUCTOR ("constructor"),
    NO ("no");

    private final String value;
    private DefaultAutowire(@NonNls String value) { this.value = value; }
    public String getValue() { return value; }
}
