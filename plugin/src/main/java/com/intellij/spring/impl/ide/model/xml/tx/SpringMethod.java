// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/tx

package com.intellij.spring.impl.ide.model.xml.tx;

import jakarta.annotation.Nonnull;

import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.Required;

/**
 * http://www.springframework.org/schema/tx:methodElemType interface.
 */
public interface SpringMethod extends SpringTxElement {

	/**
	 * Returns the value of the name child.
	 * <pre>
	 * <h3>Attribute null:name documentation</h3>
	 * 	The method name(s) with which the transaction attributes are to be
	 * 	associated. The wildcard (*) character can be used to associate the
	 * 	same transaction attribute settings with a number of methods; for
	 * 	example, 'get*', 'handle*', 'on*Event', etc.
	 * 							
	 * </pre>
	 * @return the value of the name child.
	 */
	@Nonnull
	@Required
	GenericAttributeValue<String> getName();


	/**
	 * Returns the value of the propagation child.
	 * @return the value of the propagation child.
	 */
	@Nonnull
	GenericAttributeValue<Propagation> getPropagation();


	/**
	 * Returns the value of the isolation child.
	 * @return the value of the isolation child.
	 */
	@Nonnull
	GenericAttributeValue<Isolation> getIsolation();


	/**
	 * Returns the value of the timeout child.
	 * <pre>
	 * <h3>Attribute null:timeout documentation</h3>
	 * 	The transaction timeout value (in seconds).
	 * 							
	 * </pre>
	 * @return the value of the timeout child.
	 */
	@Nonnull
	GenericAttributeValue<Integer> getTimeout();


	/**
	 * Returns the value of the read-only child.
	 * <pre>
	 * <h3>Attribute null:read-only documentation</h3>
	 * 	Is this transaction read-only?
	 * 							
	 * </pre>
	 * @return the value of the read-only child.
	 */
	@Nonnull
	GenericAttributeValue<Boolean> getReadOnly();


	/**
	 * Returns the value of the rollback-for child.
	 * <pre>
	 * <h3>Attribute null:rollback-for documentation</h3>
	 * 	The Exception(s) that will trigger rollback; comma-delimited.
	 * 	For example, 'com.foo.MyBusinessException,ServletException'
	 * 							
	 * </pre>
	 * @return the value of the rollback-for child.
	 */
	@Nonnull
	GenericAttributeValue<String> getRollbackFor();


	/**
	 * Returns the value of the no-rollback-for child.
	 * <pre>
	 * <h3>Attribute null:no-rollback-for documentation</h3>
	 * 	The Exception(s) that will *not* trigger rollback; comma-delimited.
	 * 	For example, 'com.foo.MyBusinessException,ServletException'
	 * 							
	 * </pre>
	 * @return the value of the no-rollback-for child.
	 */
	@Nonnull
	GenericAttributeValue<String> getNoRollbackFor();


}
