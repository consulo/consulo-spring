package com.intellij.spring.impl.ide.model.xml.beans;

import javax.annotation.Nonnull;

import com.intellij.spring.impl.ide.model.converters.SpringBeanScopeConverter;
import consulo.xml.util.xml.Convert;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.GenericAttributeValue;

public interface ScopedElement extends DomElement {
  /**
   * Returns the value of the scope child.
   * <pre>
   * <h3>Attribute null:scope documentation</h3>
   * 	The scope of this bean: typically "singleton" (one shared instance,
   * 	which will be returned by all calls to getBean() with the id),
   * 	or "prototype" (independent instance resulting from each call to
   * 	getBean(). Default is "singleton".
   * 	Singletons are most commonly used, and are ideal for multi-threaded
   * 	service objects. Further scopes, such as "request" or "session",
   * 	might be supported by extended bean factories (for example, in a
   * 	web environment).
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per concrete bean definition.
   * 	Inner bean definitions inherit the singleton status of their containing
   * 	bean definition, unless explicitly specified: The inner bean will be a
   * 	singleton if the containing bean is a singleton, and a prototype if
   * 	the containing bean has any other scope.
   * <p/>
   * </pre>
   *
   * @return the value of the scope child.
   */
  @Nonnull
  @Convert(value = SpringBeanScopeConverter.class)
  GenericAttributeValue<SpringBeanScope> getScope();
}