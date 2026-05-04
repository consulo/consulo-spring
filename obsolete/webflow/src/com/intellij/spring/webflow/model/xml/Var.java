package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiType;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.WebflowVarBeanConverter;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

public interface Var extends WebflowDomElement {

  @NotNull
  @Required
  @NameValue    
  GenericAttributeValue<String> getName();


  @NotNull
  @Attribute("class")
  @ExtendClass(value = CommonClassNames.JAVA_IO_SERIALIZABLE, allowInterface = false, allowAbstract = false, allowEnum = false, instantiatable = false)
  GenericAttributeValue<PsiType> getClazz();

  /**
   * <li>request - The variable goes out of scope when a call to start this flow completes.
   * <li>flash - The variable goes out of scope when the next user event is signaled.
   * <li>flow - The variable goes out of scope when this flow session ends.
   * <li>conversation - The variable goes out of scope when the overall conversation governing this flow ends.
   * </ol>
   * <br>
   * If not specified the default scope type is used ('flow' by default).
   * </pre>
   *
   * @return the value of the scope child.
   */
  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  GenericAttributeValue<Scope> getScope();


  /**
   * Returns the value of the bean child.
   * <pre>
   * <h3>Attribute null:bean documentation</h3>
   * The bean defining the initial flow variable value.  The bean *must* be a non-singleton prototype.
   * Only required if the bean name differs from the variable name.
   * </pre>
   *
   * @return the value of the bean child.
   */
  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  @Convert(WebflowVarBeanConverter.class)
  GenericAttributeValue<SpringBeanPointer> getBean();
}
