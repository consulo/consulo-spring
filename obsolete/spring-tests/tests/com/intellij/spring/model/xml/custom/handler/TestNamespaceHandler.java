/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.custom.handler;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author peter
 */
public class TestNamespaceHandler extends NamespaceHandlerSupport {
  public void init() {
    registerBeanDefinitionParser("stringBean", new AbstractSingleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        return String.class;
      }

      @Override
      protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        final String s = element.getAttribute("name");
        if (s.length() > 0) return s;

        return super.resolveId(element, definition, parserContext);
      }
    });

    registerBeanDefinitionParser("eternity", new AbstractSingleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        try {
          Thread.sleep(30 * 60 * 1000); // half an hour
        }
        catch (InterruptedException e) {
          // Ignore
        }
        return String.class;
      }
    });
    
    registerBeanDefinitionParser("exception", new BeanDefinitionParser() {
      public BeanDefinition parse(final Element element, final ParserContext parserContext) {
        throw new UnsupportedOperationException("Method parse is not yet implemented in " + getClass().getName());
      }
    });

    registerBeanDefinitionParser("outer", new BeanDefinitionParser() {
      public BeanDefinition parse(final Element element, final ParserContext parserContext) {
        final BeanDefinitionRegistry registry = parserContext.getRegistry();

        final AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(String.class).setSource(parserContext.extractSource(element)).getBeanDefinition();
        registry.registerBeanDefinition("outer", definition);

        Element inner = (Element)element.getElementsByTagName("inner").item(0);
        registry.registerBeanDefinition("inner", BeanDefinitionBuilder.rootBeanDefinition(String.class).setSource(parserContext.extractSource(inner)).getBeanDefinition());

        registry.registerBeanDefinition("unmappedInner", BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition());

        final NodeList namedInners = element.getElementsByTagNameNS(element.getNamespaceURI(), "namedInner");
        if (namedInners.getLength() > 0) {                        
          final Element namedInner = (Element)namedInners.item(0);
          registry.registerBeanDefinition(namedInner.getAttribute("name"),
                                          BeanDefinitionBuilder.rootBeanDefinition(String.class).setSource(parserContext.extractSource(namedInner)).getBeanDefinition());
        }

        return definition;
      }
    });
    registerBeanDefinitionParser("withInfrastructure", new BeanDefinitionParser() {
      public BeanDefinition parse(final Element element, final ParserContext parserContext) {
        final BeanDefinitionRegistry registry = parserContext.getRegistry();

        final AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(String.class).setSource(parserContext.extractSource(element)).getBeanDefinition();
        registry.registerBeanDefinition(element.getAttribute("id"), definition);

        final AbstractBeanDefinition infraDef = BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition();
        infraDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        registry.registerBeanDefinition("noOneShouldSeeIt", infraDef);
        return definition;
      }
    });
    registerBeanDefinitionParser("onlyInfrastructure", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        final AbstractBeanDefinition infraDef = BeanDefinitionBuilder.rootBeanDefinition(String.class).getBeanDefinition();
        infraDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        return infraDef;
      }
    });

    registerBeanDefinitionParser("factoryBean15", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestFactoryBean.class);
         builder.setSource(element);
         return builder.getBeanDefinition();
      }
    });
    registerBeanDefinitionParser("genericMethod", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GenericFactoryBean.class);
         builder.setSource(element);
         return builder.getBeanDefinition();
      }
    });
    registerBeanDefinitionParser("concreteFactoryBean", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ConcreteFactoryBean.class);
         builder.setSource(element);
         return builder.getBeanDefinition();
      }
    });
    registerBeanDefinitionParser("factoryMethodFoo", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBeanWithFactoryMethod.class, "foo");
         builder.addConstructorArg("");
         builder.setSource(element);
         return builder.getBeanDefinition();
      }
    });
    registerBeanDefinitionParser("factoryMethodBar", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBeanWithFactoryMethod.class, "bar");
         builder.addConstructorArg("");
         builder.setSource(element);
         return builder.getBeanDefinition();
      }
    });
    registerBeanDefinitionParser("factoryMethodInAnotherBean", new AbstractBeanDefinitionParser() {
      protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBeanWithFactoryMethod.class, "foo");
         builder.addConstructorArg("");
         builder.setSource(element);
         builder.setFactoryBean("xxx", "yyy");
         return builder.getBeanDefinition();
      }
    });

    registerBeanDefinitionParser("annotated", new AbstractSingleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        return String.class;
      }
    });
    registerBeanDefinitionParser("recursive", new AbstractSingleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        return String.class;
      }
    });
    registerBeanDefinitionParser("hardCoded", new AbstractSimpleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        return String.class;
      }

      protected String resolveId(final Element element, final AbstractBeanDefinition definition, final ParserContext parserContext) {
        return "hardCoded";
      }
    });
    registerBeanDefinitionParser("stringBuffer", new AbstractSimpleBeanDefinitionParser() {
      protected Class getBeanClass(final Element element) {
        return StringBuffer.class;
      }
    });
  }

}
