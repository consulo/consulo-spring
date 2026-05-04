package com.intellij.spring.webflow.el;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import static com.intellij.patterns.StandardPatterns.string;
import com.intellij.patterns.StringPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import com.intellij.psi.impl.source.jsp.el.ELLanguage;
import com.intellij.psi.impl.source.jsp.el.impl.ELResolveUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.xml.WebflowDomModelManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebflowELInjector implements MultiHostInjector {
  private static final List<Pair<String, String>> expressionTypeAttributes = new ArrayList<Pair<String, String>>();
  private static final List<XmlAttributeValuePattern> myPatterns  = new ArrayList<XmlAttributeValuePattern>();
  private WebflowDomModelManager myWebflowDomModelManager;

  static {
    expressionTypeAttributes.add(new Pair<String, String>("evaluate", "expression"));
    expressionTypeAttributes.add(new Pair<String, String>("evaluate", "result"));
    expressionTypeAttributes.add(new Pair<String, String>("view-state", "model"));
    expressionTypeAttributes.add(new Pair<String, String>("set", "name"));
    expressionTypeAttributes.add(new Pair<String, String>("set", "value"));
    expressionTypeAttributes.add(new Pair<String, String>("input", "value"));
    expressionTypeAttributes.add(new Pair<String, String>("if", "test"));
    expressionTypeAttributes.add(new Pair<String, String>("output", "value"));

    initPatterns();
  }

  private static void initPatterns() {
    for (Pair<String, String> pair : expressionTypeAttributes) {
      final String attrNames = pair.second;
      final StringPattern namePattern = string().oneOf(attrNames);

      final String tagName = pair.first;

      myPatterns.add(
        xmlAttributeValue().withLocalName(namePattern).withSuperParent(
          2,
          xmlTag().withLocalName(tagName).withNamespace(WebflowConstants.WEBFLOW_NAMESPACE)));
    }
  }

  public WebflowELInjector(Project project) {
    myWebflowDomModelManager = WebflowDomModelManager.getInstance(project);
  }

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    if (!(host instanceof XmlAttributeValue)) return;

    final XmlAttributeValue originalElement = (XmlAttributeValue)PsiUtil.getOriginalElement(host, XmlAttributeValue.class);

    if (originalElement == null) return;
    if (originalElement.getText().length() < 2) return; // IDEADEV-29268

    if (acceptExpressionXmlAttributeValue(originalElement)) {
      registrar.startInjecting(ELLanguage.INSTANCE)
          .addPlace(null, null, (PsiLanguageInjectionHost)host, getTextRange(host)).doneInjecting();

      host.putUserData(ELContextProvider.ourContextProviderKey, new WebflowELExpressionContextProvider(host));
    } else if (isWebflowFile(originalElement.getContainingFile())) {
      for (TextRange textRange : ELResolveUtil.getELTextRanges(originalElement, "${", "}")) {
        registrar.startInjecting(ELLanguage.INSTANCE).addPlace(null, null, (PsiLanguageInjectionHost)originalElement, textRange)
            .doneInjecting();
      }
      originalElement.putUserData(ELContextProvider.ourContextProviderKey, new WebflowELExpressionContextProvider(originalElement));
    }
  }

  private boolean isWebflowFile(final PsiFile file) {
    return file instanceof XmlFile && myWebflowDomModelManager.isWebflow((XmlFile)file);
  }

  private static TextRange getTextRange(final PsiElement host) {
    final int length = ((XmlAttributeValue)host).getValue().length();

    return TextRange.from(1, length);
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }

  private static boolean acceptExpressionXmlAttributeValue(final XmlAttributeValue value) {
    for (XmlAttributeValuePattern myPattern : myPatterns) {
      if (myPattern.accepts(value)) return true;
    }
    return false;
  }
}
