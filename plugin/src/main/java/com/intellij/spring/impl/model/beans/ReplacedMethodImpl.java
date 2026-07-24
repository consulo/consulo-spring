package com.intellij.spring.impl.model.beans;

import com.intellij.spring.impl.ide.model.xml.beans.ReplacedMethod;
import consulo.util.lang.ComparatorUtil;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ReplacedMethodImpl implements ReplacedMethod {

  public int hashCode() {
    String value = getName().getStringValue();
    return value == null ? 0 : value.hashCode();
  }

  public boolean equals(Object obj) {
    return obj instanceof ReplacedMethod &&
           ComparatorUtil.equalsNullable(getName().getStringValue(), ((ReplacedMethod)obj).getName().getStringValue());
  }
}
