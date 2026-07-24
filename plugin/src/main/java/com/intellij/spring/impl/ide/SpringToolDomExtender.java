/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide;

import com.intellij.java.impl.util.xml.DomJavaUtil;
import com.intellij.java.impl.util.xml.ExtendClassImpl;
import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.beans.MetadataPropertyValueConverter;
import com.intellij.spring.impl.ide.model.xml.beans.MetadataRefValue;
import com.intellij.spring.impl.ide.model.xml.beans.MetadataValue;
import com.intellij.xml.impl.schema.XmlAttributeDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.component.bind.ParameterizedTypeImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.xml.descriptor.XmlAttributeDescriptor;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.language.XmlName;
import consulo.xml.dom.reflect.DomExtender;
import consulo.xml.dom.reflect.DomExtension;
import consulo.xml.dom.reflect.DomExtensionsRegistrar;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringToolDomExtender extends DomExtender<CustomBeanWrapper> {

  @Nullable
  public static XmlTag getToolAnnotationTag(@Nullable PsiElement declaration, boolean allowRecursion) {
    if (declaration instanceof XmlTag) {
      XmlTag xmlTag = (XmlTag)declaration;
      XmlTag[] tags = xmlTag.findSubTags("annotation", XmlUtil.XML_SCHEMA_URI);
      if (tags.length > 0) {
        XmlTag[] tags1 = tags[0].findSubTags("appinfo", XmlUtil.XML_SCHEMA_URI);
        if (tags1.length > 0) {
          XmlTag[] tags2 = tags1[0].findSubTags("annotation", SpringConstants.TOOL_NAMESPACE);
          if (tags2.length > 0) {
            return tags2[0];
          }
        }
      }
      XmlAttribute attribute = xmlTag.getAttribute("type");
      if (allowRecursion && attribute != null) {
        XmlAttributeValue value = attribute.getValueElement();
        if (value != null) {
          for (PsiReference reference : value.getReferences()) {
            PsiElement element = reference.resolve();
            if (element instanceof XmlTag) {
              XmlTag annotationTag = getToolAnnotationTag(element, false);
              if (annotationTag != null) {
                return annotationTag;
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public Class<CustomBeanWrapper> getElementClass() {
    return CustomBeanWrapper.class;
  }

  public void registerExtensions(@Nonnull CustomBeanWrapper element, @Nonnull DomExtensionsRegistrar registrar) {
    XmlTag tag = element.getXmlTag();
    assert tag != null;
    for (XmlAttribute attribute : tag.getAttributes()) {
      XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      if (descriptor instanceof XmlAttributeDescriptorImpl) {
        XmlTag annotationTag = getToolAnnotationTag(descriptor.getDeclaration(), true);
        if (annotationTag != null) {
          boolean ref = "ref".equals(annotationTag.getAttributeValue("kind"));
          PsiClass expectedTypeClass = getExpectedTypeClass(element, annotationTag);
          if (expectedTypeClass != null) {
            final PsiClassType
              expectedType = JavaPsiFacade.getInstance(expectedTypeClass.getProject()).getElementFactory().createType(expectedTypeClass);
            XmlName xmlName = new XmlName(attribute.getName());
            if (ref) {
              registrar.registerAttributeChildExtension(xmlName, MetadataRefValue.class).setConverter(new SpringBeanResolveConverter() {
                @Nullable
                public List<PsiClassType> getRequiredClasses(ConvertContext context) {
                  return Arrays.asList(expectedType);
                }
              });
            }
            else {
              if (CommonClassNames.JAVA_LANG_CLASS.equals(expectedType.getCanonicalText())) {
                DomExtension extension = registrar.registerAttributeChildExtension(xmlName, new ParameterizedTypeImpl
                  (GenericAttributeValue.class, PsiClass.class));
                XmlTag[] tags1 = annotationTag.findSubTags("assignable-to", SpringConstants.TOOL_NAMESPACE);
                if (tags1.length > 0) {
                  final String assignableFrom = tags1[0].getAttributeValue("type");
                  if (assignableFrom != null) {
                    extension.addCustomAnnotation(new ExtendClassImpl() {
                      public String value() {
                        return assignableFrom;
                      }
                    });
                  }
                }
              }
              else {
                registrar.registerAttributeChildExtension(xmlName, MetadataValue.class)
                         .setConverter(new MetadataPropertyValueConverter(expectedType));
              }
            }
          }
        }
      }
    }
  }

  private static PsiClass getExpectedTypeClass(CustomBeanWrapper element, XmlTag annotationTag) {
    XmlFile file = DomUtil.getFile(element);
    XmlTag[] expectedTypeTags = annotationTag.findSubTags("expected-type", SpringConstants.TOOL_NAMESPACE);
    String value = expectedTypeTags.length > 0 ? expectedTypeTags[0].getAttributeValue("type") : null;
    return DomJavaUtil.findClass(value != null ? value : CommonClassNames.JAVA_LANG_OBJECT, file, element.getModule(), null);
  }


}