// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/lang

package com.intellij.spring.impl.ide.model.xml.lang;

import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.beans.Identified;
import com.intellij.spring.impl.ide.model.xml.beans.ScopedElement;
import com.intellij.spring.impl.ide.model.values.converters.ResourceValueConverter;
import consulo.xml.util.xml.*;
import javax.annotation.Nonnull;

import java.util.List;

/**
 * http://www.springframework.org/schema/lang:simpleScriptType interface.
 */
@Namespace(SpringConstants.LANG_NAMESPACE_KEY)
public interface SimpleScript extends DomElement, ScopedElement, Identified {

	/**
	 * Returns the value of the refresh-check-delay child.
	 * <pre>
	 * <h3>Attribute null:refresh-check-delay documentation</h3>
	 * 	The delay (in milliseconds) between checks for updated sources when
	 * 	using the refreshable beans feature.
	 * 						
	 * </pre>
	 * @return the value of the refresh-check-delay child.
	 */
	@Nonnull
	GenericAttributeValue<Integer> getRefreshCheckDelay();


	/**
	 * Returns the value of the script-source child.
	 * <pre>
	 * <h3>Attribute null:script-source documentation</h3>
	 * 	The resource containing the script for the dynamic language-backed bean.
	 * 	
	 * 	Examples might be '/WEB-INF/scripts/Anais.groovy', 'classpath:Nin.bsh', etc.
	 * 						
	 * </pre>
	 * @return the value of the script-source child.
	 */
	@Nonnull
  @Referencing(value = ResourceValueConverter.class)
  GenericAttributeValue<String> getScriptSource();


	/**
	 * Returns the value of the inline-script child.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/lang:inline-script documentation</h3>
	 * 	The source code for the dynamic language-backed bean.
	 * 							
	 * </pre>
	 * @return the value of the inline-script child.
	 */
	@Nonnull
	GenericDomValue<String> getInlineScript();


	/**
	 * Returns the list of property children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/lang:property documentation</h3>
	 * 	Dynamic language-backed bean definitions can have zero or more properties.
	 * 	Property elements correspond to JavaBean setter methods exposed
	 * 	by the bean classes. Spring supports primitives, references to other
	 * 	beans in the same or related factories, lists, maps and properties.
	 * 							
	 * </pre>
	 * @return the list of property children.
	 */
	@Nonnull
	List<LangProperty> getProperties();
	/**
	 * Adds new child to the list of property children.
	 * @return created child
	 */
	LangProperty addProperty();


}
