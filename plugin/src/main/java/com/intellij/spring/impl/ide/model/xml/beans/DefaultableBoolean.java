// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans
package com.intellij.spring.impl.ide.model.xml.beans;

import consulo.xml.dom.NamedEnum;
import org.jetbrains.annotations.NonNls;

/**
 * http://www.springframework.org/schema/beans:defaultable-boolean enumeration.
 */
public enum DefaultableBoolean implements NamedEnum {
    DEFAULT("default"),
    FALSE("false"),
    TRUE("true");

    private final String value;

    private DefaultableBoolean(@NonNls String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
