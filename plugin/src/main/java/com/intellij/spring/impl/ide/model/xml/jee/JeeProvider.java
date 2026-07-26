// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/jee
package com.intellij.spring.impl.ide.model.xml.jee;

/**
 * http://www.springframework.org/schema/jee:providerAttrType enumeration.
 */
public enum JeeProvider implements consulo.xml.dom.NamedEnum {
    HIBERNATE("hibernate"),
    KODO("kodo"),
    TOPLINK("toplink");

    private final String value;

    private JeeProvider(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
