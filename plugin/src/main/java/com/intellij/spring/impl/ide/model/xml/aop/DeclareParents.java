// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/aop
package com.intellij.spring.impl.ide.model.xml.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.psi.AopReferenceHolder;
import com.intellij.java.impl.util.xml.ClassTemplate;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.ClassKind;
import consulo.xml.dom.Convert;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.Required;

import jakarta.annotation.Nonnull;

/**
 * http://www.springframework.org/schema/aop:declareParentsType interface.
 */
public interface DeclareParents extends SpringAopElement, AopIntroduction {
    /**
     * Returns the value of the types-matching child.
     * <pre>
     * <h3>Attribute null:types-matching documentation</h3>
     * 	The AspectJ type expression that defines what types (classes) the
     * 	introduction is restricted to.
     *
     * 	An example would be 'org.springframework.beans.ITestBean+'.
     *
     * </pre>
     *
     * @return the value of the types-matching child.
     */
    @Convert(TypesMatchingConverter.class)
    @Nonnull
    @Override
    @Required
    GenericAttributeValue<AopReferenceHolder> getTypesMatching();

    /**
     * Returns the value of the implement-interface child.
     * <pre>
     * <h3>Attribute null:implement-interface documentation</h3>
     * 	The fully qualified name of the interface that will be introduced.
     *
     * </pre>
     *
     * @return the value of the implement-interface child.
     */
    @ClassTemplate(kind = ClassKind.INTERFACE)
    @Nonnull
    @Override
    @Required
    GenericAttributeValue<PsiClass> getImplementInterface();

    /**
     * Returns the value of the default-impl child.
     * <pre>
     * <h3>Attribute null:default-impl documentation</h3>
     * 	The fully qualified name of the class that will be instantiated to serve
     * 	as the default implementation of the introduced interface.
     *
     * </pre>
     *
     * @return the value of the default-impl child.
     */
    @Nonnull
    @Override
    GenericAttributeValue<PsiClass> getDefaultImpl();
}
