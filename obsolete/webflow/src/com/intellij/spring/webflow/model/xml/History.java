// Generated on Tue May 27 14:10:39 MSD 2008
// DTD/Schema  :    http://www.springframework.org/schema/webflow

package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.NamedEnum;

@ModelVersion(WebflowVersion.Webflow_2_0)
public enum History implements NamedEnum {
  DISCARD("discard"),
  INVALIDATE("invalidate"),
  PRESERVE("preserve");

  private final String value;

  private History(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}