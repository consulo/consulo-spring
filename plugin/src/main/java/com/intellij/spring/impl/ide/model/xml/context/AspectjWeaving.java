// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context
package com.intellij.spring.impl.ide.model.xml.context;

/**
 * http://www.springframework.org/schema/context:aspectj-weavingAttrType enumeration.
 */
public enum AspectjWeaving implements consulo.xml.dom.NamedEnum {
    AUTODETECT("autodetect"),
    OFF("off"),
    ON("on");

    private final String value;

    private AspectjWeaving(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
