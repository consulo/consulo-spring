// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.impl.ide.model.values.EntryKeyConverter;
import consulo.xml.util.xml.Attribute;
import consulo.xml.util.xml.Convert;
import consulo.xml.util.xml.GenericAttributeValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * http://www.springframework.org/schema/beans:entryType interface.
 */
public interface SpringEntry extends SpringValueHolder {

  @Nullable
  PsiClass getRequiredKeyClass();

  @Nullable
  PsiType getRequiredKeyType();

  /**
   * Returns the value of the key child.
   * <pre>
   * <h3>Attribute null:key documentation</h3>
   * 	Each map element must specify its key as attribute or as child element.
   * 	A key attribute is always a String value.
   *
   * </pre>
   *
   * @return the value of the key child.
   */
  @Nonnull
  @Attribute(value = "key")
  @Convert(EntryKeyConverter.class)
  GenericAttributeValue<String> getKeyAttr();


  /**
   * Returns the value of the key-ref child.
   * <pre>
   * <h3>Attribute null:key-ref documentation</h3>
   * 	A short-cut alternative to a to a "key" element with a nested
   * 	"<ref bean='...'/>".
   *
   * </pre>
   *
   * @return the value of the key-ref child.
   */
  @Convert(value = SpringBeanResolveConverter.Key.class)
  @Nonnull
  GenericAttributeValue<SpringBeanPointer> getKeyRef();

  /**
   * Returns the value of the value-ref child.
   * <pre>
   * <h3>Attribute null:value-ref documentation</h3>
   * 	A short-cut alternative to a nested "<ref bean='...'/>".
   *
   * </pre>
   *
   * @return the value of the value-ref child.
   */
  @Nonnull
  @Attribute(value = "value-ref")
  @Convert(value = SpringBeanResolveConverter.PropertyBean.class)
  GenericAttributeValue<SpringBeanPointer> getValueRef();

  /**
   * Returns the value of the key child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:key documentation</h3>
   * 	A key element can contain an inner bean, ref, value, or collection.
   *
   * </pre>
   *
   * @return the value of the key child.
   */
  @Nonnull
  SpringKey getKey();
}
