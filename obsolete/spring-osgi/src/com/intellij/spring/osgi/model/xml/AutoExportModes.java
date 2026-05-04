package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum AutoExportModes implements NamedEnum {
	ALL_CLASSES ("all-classes"),
	CLASS_HIERARCHY ("class-hierarchy"),
	DISABLED ("disabled"),
	INTERFACES ("interfaces");

	private final String value;
	private AutoExportModes(String value) { this.value = value; }

  public String getValue() { return value; }

}
