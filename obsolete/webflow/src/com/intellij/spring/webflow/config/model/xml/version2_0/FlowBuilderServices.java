package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.converters.ConversationServiceConverter;
import com.intellij.spring.webflow.config.model.xml.converters.ExpressionParserConverter;
import com.intellij.spring.webflow.config.model.xml.converters.FormatterRegistryConverter;
import com.intellij.spring.webflow.config.model.xml.converters.ViewFactoryCreatorConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface FlowBuilderServices extends WebflowConfigDomElement, DomSpringBean {
  @NonNls String FLOW_BUILDER_SERVICES_CLASS = "org.springframework.webflow.engine.builder.support.FlowBuilderServices";
  
  @NonNls String CONVERSATION_SERVICE_CLASS = "org.springframework.binding.convert.ConversionService";
  @NonNls String EXPRESSION_PARSER_CLASS = "org.springframework.binding.expression.ExpressionParser";
  @NonNls String VIEW_FACTORY_CREATOR_CLASS = "org.springframework.webflow.engine.builder.ViewFactoryCreator";
  @NonNls String FORMATTER_REGISTRY_CLASS = "org.springframework.binding.format.FormatterRegistry";


  @NotNull
  @Convert(ConversationServiceConverter.class)
  GenericAttributeValue<SpringBeanPointer> getConversionService();

  @NotNull
  @Convert(ExpressionParserConverter.class)
  GenericAttributeValue<SpringBeanPointer> getExpressionParser();

  @NotNull
  @Convert(ViewFactoryCreatorConverter.class)
  GenericAttributeValue<SpringBeanPointer> getViewFactoryCreator();

  @NotNull
  @Convert(FormatterRegistryConverter.class)    
  GenericAttributeValue<SpringBeanPointer> getFormatterRegistry();
}
