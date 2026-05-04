package com.intellij.spring.security.references;

import com.intellij.patterns.PsiJavaPatterns;
import static com.intellij.patterns.PsiJavaPatterns.literalExpression;
import static com.intellij.patterns.PsiJavaPatterns.psiNewExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ScopeFilter;
import com.intellij.psi.filters.AndFilter;
import com.intellij.psi.filters.XmlTagFilter;
import com.intellij.psi.filters.position.ParentElementFilter;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.constants.SpringSecurityConstants;
import com.intellij.xml.util.XmlUtil;

public class SpringSecurityReferenceContributor extends PsiReferenceContributor {

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    //@Secured("ROLE_USER") and new GrantedAuthorityImpl("ROLE_USER")
    registrar.registerReferenceProvider(
      literalExpression().withSuperParent(2, psiNewExpression(SpringSecurityClassesConstants.GRANTED_AUTHORITY)),
                                 new SpringSecurityRolePsiReferenceProvider.PsiLiteralExpressionProvider(), PsiReferenceRegistrar.HIGHER_PRIORITY);

    registrar.registerReferenceProvider(
      literalExpression().inside(PsiJavaPatterns.psiAnnotation().qName(SpringSecurityConstants.SECURED_ANNOTATION)),
      new SpringSecurityRolePsiReferenceProvider.PsiLiteralExpressionProvider(), PsiReferenceRegistrar.HIGHER_PRIORITY);

   // security.tld references
    XmlUtil.registerXmlAttributeValueReferenceProvider(registrar, new String[]{"ifAllGranted", "ifAnyGranted", "ifNotGranted" }, new ScopeFilter(
      new ParentElementFilter(new AndFilter(XmlTagFilter.INSTANCE, new NamespaceFilter(SpringSecurityConstants.SECURITY_TAGS_NAMESPACE)), 2)),
                                                       new SpringSecurityRolePsiReferenceProvider.XmlAttributeValueProvider());

  }

}