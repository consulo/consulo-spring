// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop
package com.intellij.spring.impl.ide.model.xml.aop;

/**
 * http://www.springframework.org/schema/aop:typeAttrType enumeration.
 */
public enum PointcutType implements consulo.xml.dom.NamedEnum {
    ASPECTJ("aspectj"),
    REGEX("regex");

    private final String value;

    private PointcutType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
