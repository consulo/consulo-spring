// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.impl.ide.model.xml.context;

import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.Required;
import javax.annotation.Nonnull;

/**
 * http://www.springframework.org/schema/context:filterType interface.
 */
public interface Filter extends DomSpringBean, SpringContextElement {

	/**
	 * Returns the value of the type child.
	 * @return the value of the type child.
	 */
	@Nonnull
	@Required
	GenericAttributeValue<Type> getType();


	/**
	 * Returns the value of the expression child.
	 * @return the value of the expression child.
	 */
	@Nonnull
	@Required
	GenericAttributeValue<String> getExpression();


}
