// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop
package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.aop.AopAspect;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.TypedBeanPointerAttribute;
import consulo.xml.dom.Convert;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.Required;
import jakarta.annotation.Nonnull;

/**
 * http://www.springframework.org/schema/aop:advisorType interface.
 */
public interface Advisor extends SpringAopElement, DomSpringBean, SpringAopAdvice, AopAspect {
    /**
     * Returns the value of the advice-ref child.
     * <pre>
     * <h3>Attribute null:advice-ref documentation</h3>
     * 	A reference to an advice bean.
     *
     * </pre>
     *
     * @return the value of the advice-ref child.
     */
    @Nonnull
    @Required
    @RequiredBeanType("org.aopalliance.aop.Advice")
    TypedBeanPointerAttribute getAdviceRef();


    /**
     * Returns the value of the pointcut child.
     * <pre>
     * <h3>Attribute null:pointcut documentation</h3>
     * 	A pointcut expression.
     *
     * </pre>
     *
     * @return the value of the pointcut child.
     */
    @Nonnull
    @Convert(PointcutExpressionConverter.class)
    GenericAttributeValue<PsiPointcutExpression> getPointcut();


    /**
     * Returns the value of the pointcut-ref child.
     * <pre>
     * <h3>Attribute null:pointcut-ref documentation</h3>
     * 	A reference to a pointcut definition.
     *
     * </pre>
     *
     * @return the value of the pointcut-ref child.
     */
    @Nonnull
    GenericAttributeValue<SpringPointcut> getPointcutRef();


    /**
     * Returns the value of the order child.
     * <pre>
     * <h3>Attribute null:order documentation</h3>
     * 	Controls the ordering of the execution of this advice when multiple
     * 	advice executes at a specific joinpoint.
     *
     * </pre>
     *
     * @return the value of the order child.
     */
    @Nonnull
    @Override
    GenericAttributeValue<Integer> getOrder();

    @Nonnull
    @Override
    SpringAdvisedElementsSearcher getSearcher();
}
