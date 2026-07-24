package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.context.AnnotationConfig;
import consulo.xml.language.psi.XmlTag;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;

/**
 * @author Sergey.Vasiliev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class AnnotationConfigImpl extends DomSpringBeanImpl implements AnnotationConfig {
    @Override
    public String getBeanName() {
        XmlTag tag = getXmlTag();
        return tag == null ? "context:annotation-config" : tag.getName();
    }

    @Nullable
    @Override
    public String getClassName() {
        return null;
    }
}