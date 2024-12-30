// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop

package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.aop.AopAfterThrowingAdvice;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.xml.util.xml.Convert;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.Required;

import jakarta.annotation.Nonnull;

/**
 * http://www.springframework.org/schema/aop:afterThrowingAdviceType interface.
 */
public interface AfterThrowingAdvice extends DomElement, BasicAdvice, AopAfterThrowingAdvice {

	/**
	 * Returns the value of the throwing child.
	 * <pre>
	 * <h3>Attribute null:throwing documentation</h3>
	 * 	The name of the method parameter to which the thrown exception must
	 * 	be passed.	
	 * 						
	 * </pre>
	 * @return the value of the throwing child.
	 */
	@Nonnull
        @Convert(value=AdviceParameterConverter.class, soft=true)
        GenericAttributeValue<PsiParameter> getThrowing();


	/**
	 * Returns the value of the pointcut child.
	 * <pre>
	 * <h3>Attribute null:pointcut documentation</h3>
	 * 	The associated pointcut expression.
	 * 				
	 * </pre>
	 * @return the value of the pointcut child.
	 */
	@Nonnull
	GenericAttributeValue<PsiPointcutExpression> getPointcut();


	/**
	 * Returns the value of the pointcut-ref child.
	 * <pre>
	 * <h3>Attribute null:pointcut-ref documentation</h3>
	 * 	The name of an associated pointcut definition.
	 * 				
	 * </pre>
	 * @return the value of the pointcut-ref child.
	 */
	@Nonnull
	GenericAttributeValue<SpringPointcut> getPointcutRef();


	/**
	 * Returns the value of the method child.
	 * <pre>
	 * <h3>Attribute null:method documentation</h3>
	 * 	The name of the method that defines the logic of the advice.
	 * 				
	 * </pre>
	 * @return the value of the method child.
	 */
	@Nonnull
	@Required
	GenericAttributeValue<PsiMethod> getMethod();


	/**
	 * Returns the value of the arg-names child.
	 * <pre>
	 * <h3>Attribute null:arg-names documentation</h3>
	 * 	The comma-delimited list of advice method argument (parameter) names 
	 * 	that will be matched from pointcut parameters.
	 * 				
	 * </pre>
	 * @return the value of the arg-names child.
	 */
	@Nonnull
	GenericAttributeValue<String> getArgNames();


}
