package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.psi.PsiFile;
import com.intellij.spring.model.converters.SpringResourceConverter;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow-config:locationType interface.
 */
public interface Location extends WebflowConfigDomElement {

  /**
   * Returns the value of the path child.
   * <pre>
   * <h3>Attribute null:path documentation</h3>
   * The path to the externalized flow definition resource.  May be a path to a single resource or
   * a ANT-style path expression that matches multiple resources.
   * </pre>
   *
   * @return the value of the path child.
   */
  @NotNull
  @Required
  @Convert(SpringResourceConverter.class)
  GenericAttributeValue<PsiFile> getPath();
}
