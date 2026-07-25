// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans
package com.intellij.spring.impl.ide.model.xml.beans;

import consulo.xml.dom.NamedEnum;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;

/**
 * http://www.springframework.org/schema/beans:dependency-checkAttrType enumeration.
 */
public enum DependencyCheck implements NamedEnum {
    ALL("all"),
    DEFAULT("default"),
    NONE("none"),
    OBJECTS("objects"),
    SIMPLE("simple");

    private final String value;

    private DependencyCheck(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public static DependencyCheck fromDefault(@Nullable DefaultDependencyCheck def) {
        return switch (def) {
            case null -> DEFAULT;
            case ALL -> ALL;
            case NONE -> NONE;
            case OBJECTS -> OBJECTS;
            case SIMPLE -> SIMPLE;
            default -> DEFAULT;
        };
    }
}
