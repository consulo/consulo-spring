package com.intellij.spring.impl.ide.model.structure;

import com.intellij.java.impl.psi.impl.beanProperties.BeanProperty;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValueHolderDefinition;
import consulo.application.AllIcons;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import consulo.xml.dom.DomElementNavigationProvider;
import consulo.xml.dom.DomElementsNavigationManager;
import consulo.xml.dom.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public class SpringInjectionTreeElement implements StructureViewTreeElement, ItemPresentation {

  private final SpringValueHolderDefinition myInjection;
  @NonNls
  private static final String CONSTRUCTOR_ARG = "constructor-arg";
  @NonNls
  private static final String PROPERTY_TAG = "property";

  public SpringInjectionTreeElement(@Nonnull SpringValueHolderDefinition injection) {
    myInjection = injection;
  }

  public Object getValue() {
    return myInjection.getXmlElement();
  }

  public ItemPresentation getPresentation() {
    return this;
  }

  public String getPresentableText() {
    if (isConstructorArg()) {
      return CONSTRUCTOR_ARG;
    }
    else {
      SpringProperty springProperty = (SpringProperty)myInjection;
      String propertyName = springProperty.getName().getStringValue();
      if (propertyName == null) propertyName = PROPERTY_TAG;
      PsiType type = getPropertyType(springProperty);
      if (type != null) {
        propertyName += ": " + type.getCanonicalText();
      }
      return propertyName;
    }
  }

  @Nullable
  private static PsiType getPropertyType(SpringProperty property) {
    List<BeanProperty> value = property.getName().getValue();
    if (value != null && !value.isEmpty()) {
      return value.get(0).getPropertyType();
    }
    else {
      PsiType[] psiTypes = property.getTypesByValue();
      return psiTypes == null || psiTypes.length == 0 ? null : psiTypes[0];
    }
  }

  public String getLocationString() {
    String value = getValueText();
    if (value != null) return "value=\"" + value + "\"";
    value = getRefText();
    if (value != null) return "ref=\"" + value + "\"";

    return null;
  }

  public Image getIcon() {
    return isConstructorArg() ? AllIcons.Nodes.Method : SpringIcons.SPRING_BEAN_PROPERTY_ICON;
  }

  public TreeElement[] getChildren() {
    return new TreeElement[0];
  }

  public void navigate(boolean requestFocus) {
    DomElementNavigationProvider navigationProvider = DomElementsNavigationManager.getManager(myInjection.getManager().getProject())
                                                                                        .getDomElementsNavigateProvider(
                                                                                          DomElementsNavigationManager.DEFAULT_PROVIDER_NAME);

    navigationProvider.navigate(myInjection, requestFocus);
  }

  public boolean canNavigate() {
    return myInjection.isValid();
  }

  public boolean canNavigateToSource() {
    return myInjection.isValid();
  }

  public boolean isConstructorArg() {
    return myInjection instanceof ConstructorArg;
  }

  @Nullable
  private String getValueText() {
    GenericDomValue<?> element = myInjection.getValueElement();
    return element == null ? null : element.getStringValue();
  }

  @Nullable
  private String getRefText() {
    GenericDomValue<SpringBeanPointer> refElement = myInjection.getRefElement();
    return refElement != null ? refElement.getStringValue() : null;
  }
}
