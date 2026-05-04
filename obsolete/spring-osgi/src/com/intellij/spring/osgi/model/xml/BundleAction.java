package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.NamedEnum;

public enum BundleAction implements NamedEnum {
	INSTALL ("install"),
	START ("start"),
	STOP ("stop"),
	UNINSTALL ("uninstall"),
	UPDATE ("update");

	private final String value;
	private BundleAction(String value) { this.value = value; }

  public String getValue() { return value; }

}
