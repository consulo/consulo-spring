// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop
package com.intellij.spring.impl.ide.model.xml.aop;

import consulo.xml.dom.GenericAttributeValue;
import com.intellij.aop.AopModel;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * http://www.springframework.org/schema/aop:configElemType interface.
 */
public interface AopConfig extends SpringAopElement, AopModel {
	/**
	 * Returns the value of the proxy-target-class child.
	 * <pre>
	 * <h3>Attribute null:proxy-target-class documentation</h3>
	 * 	Are class-based (CGLIB) proxies to be created? By default, standard
	 * 	Java interface-based proxies are created.
	 * 					
	 * </pre>
	 * @return the value of the proxy-target-class child.
	 */
	@Nonnull
	GenericAttributeValue<Boolean> getProxyTargetClass();

	/**
	 * Returns the list of pointcut children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:pointcut documentation</h3>
	 * 	A named pointcut definition.
	 * 						
	 * </pre>
	 * @return the list of pointcut children.
	 */
	@Nonnull
    @Override
	List<SpringPointcut> getPointcuts();

	/**
	 * Adds new child to the list of pointcut children.
	 * @return created child
	 */
	SpringPointcut addPointcut();

	/**
	 * Returns the list of advisor children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:advisor documentation</h3>
	 * 	A named advisor definition.
	 * 						
	 * </pre>
	 * @return the list of advisor children.
	 */
	@Nonnull
	List<Advisor> getAdvisors();

	/**
	 * Adds new child to the list of advisor children.
	 * @return created child
	 */
	Advisor addAdvisor();

	/**
	 * Returns the list of aspect children.
	 * <pre>
	 * <h3>Element http://www.springframework.org/schema/aop:aspect documentation</h3>
	 * 	A named aspect definition.
	 * 						
	 * </pre>
	 * @return the list of aspect children.
	 */
	@Nonnull
    @Override
	List<SpringAspect> getAspects();

	/**
	 * Adds new child to the list of aspect children.
	 * @return created child
	 */
	SpringAspect addAspect();
}
