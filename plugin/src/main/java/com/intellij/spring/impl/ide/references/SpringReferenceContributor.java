package com.intellij.spring.impl.ide.references;

import com.intellij.java.impl.psi.filters.PsiMethodCallFilter;
import com.intellij.java.impl.psi.filters.TextFilter;
import com.intellij.java.language.patterns.PsiJavaPatterns;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.xml.util.XmlUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.java.properties.impl.psi.PropertiesReferenceProvider;
import consulo.language.Language;
import consulo.language.pattern.FilterPattern;
import consulo.language.pattern.PatternCondition;
import consulo.language.psi.*;
import consulo.language.psi.filter.AndFilter;
import consulo.language.psi.filter.ScopeFilter;
import consulo.language.psi.filter.position.ParentElementFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.language.psi.pattern.XmlPatterns;
import consulo.xml.psi.filters.XmlTagFilter;
import consulo.xml.psi.filters.position.NamespaceFilter;
import consulo.xml.dom.DomUtil;

import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringReferenceContributor extends PsiReferenceContributor {
  public static final String SPRING_URI = "http://www.springframework.org/tags";

  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    PsiReferenceProvider propertiesReferenceProvider = new PropertiesReferenceProvider(false);

    //duplicated in FtlLiteralExpression
    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{"code"}, new ScopeFilter(new ParentElementFilter(
      new AndFilter(new NamespaceFilter(SPRING_URI),
                    new AndFilter(XmlTagFilter.INSTANCE, new TextFilter("message", "theme"))), 2)), propertiesReferenceProvider);

    registrar.registerReferenceProvider(PsiJavaPatterns.literalExpression()
                                                       .and(new FilterPattern(new ParentElementFilter(new PsiMethodCallFilter(
                                                         "org.springframework.beans.factory.BeanFactory",
                                                         SpringBeanNamesReferenceProvider.METHODS), 2))),
                                        new SpringBeanNamesReferenceProvider());

    registrar.registerReferenceProvider(XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("customBeanId") {
      public boolean accepts(@Nonnull XmlAttributeValue attributeValue, ProcessingContext context) {
        DomSpringBean element = DomUtil.findDomElement(attributeValue, DomSpringBean.class);
        if (element instanceof CustomBeanWrapper) {
          for (CustomBean customBean : ((CustomBeanWrapper)element).getCustomBeans()) {
            if (customBean.getIdAttribute() == attributeValue.getParent()) {
              context.put("bean", customBean);
              return true;
            }
          }
        }
        return false;
      }
    }), new PsiReferenceProvider() {
      @Nonnull
      public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext context) {
        CustomBean bean = (CustomBean)context.get("bean");
        return new PsiReference[]{PsiReferenceBase.createSelfReference(element, bean.getIdentifyingPsiElement())};
      }
    });

    registrar.registerReferenceProvider(
      PsiJavaPatterns.literalExpression().annotationParam(SpringAnnotationsConstants.JAVAX_RESOURCE_ANNOTATION, "name"),
      new PsiReferenceProvider() {
        @Nonnull
        @Override
        public PsiReference[] getReferencesByElement(@Nonnull final PsiElement element, @Nonnull ProcessingContext context) {
          PsiMember member = PsiTreeUtil.getParentOfType(element, PsiMember.class);
          PsiType type = PropertyUtil.getPropertyType(member);
          final PsiClass required = type instanceof PsiClassType ? ((PsiClassType)type).resolve() : null;
          return new PsiReference[]{new SpringBeanReference((PsiLiteralExpression)element, required) {
            @Override
            public boolean isSoft() {
              return true;
            }
          }
          };
        }
      },
      PsiReferenceRegistrar.HIGHER_PRIORITY);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return Language.ANY;
  }
}
