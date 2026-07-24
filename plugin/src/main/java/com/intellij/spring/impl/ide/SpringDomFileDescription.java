/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide;

import com.intellij.aop.AopBundle;
import com.intellij.aop.psi.AopPointcutTypes;
import com.intellij.aop.psi.PointcutDescriptor;
import com.intellij.spring.impl.model.aop.psi.PsiBeanPointcutExpression;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.component.ExtensionImpl;
import consulo.component.util.Iconable;
import consulo.language.ast.ASTNode;
import consulo.language.pratt.PrattBuilder;
import consulo.spring.impl.SpringIcons;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileDescription;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.intellij.aop.psi.AopElementTypes.AOP_LEFT_PAR;
import static com.intellij.aop.psi.AopElementTypes.AOP_RIGHT_PAR;
import static com.intellij.aop.psi.AopPrattParser.parsePatternPart;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringDomFileDescription extends DomFileDescription<Beans> {
  static {
    // TODO dirty hack - need extension for it
    AopPointcutTypes.registerPointcut(new PointcutDescriptor("bean") {
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
          parsePatternPart(builder);
          builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        }
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiBeanPointcutExpression(node);
      }
    });
  }

  private static final List<String> SPRING_NAMESPACES =
    Arrays.asList(SpringConstants.BEANS_DTD_1, SpringConstants.BEANS_DTD_2, SpringConstants.BEANS_XSD);

  public SpringDomFileDescription() {
    super(Beans.class, "beans");
  }

  @Nullable
  @Override
  public Image getFileIcon(@Iconable.IconFlags int flags) {
    return SpringIcons.SpringConfig;
  }

  @Override
  public boolean isMyFile(@Nonnull XmlFile file) {
    return true;
  }

  @Override
  protected void initializeFileDescription() {
    registerNamespacePolicy(SpringConstants.BEANS_NAMESPACE_KEY, tag -> SPRING_NAMESPACES);

    registerNamespacePolicy(SpringConstants.AOP_NAMESPACE_KEY, SpringConstants.AOP_NAMESPACE);
    registerNamespacePolicy(SpringConstants.JEE_NAMESPACE_KEY, SpringConstants.JEE_NAMESPACE);
    registerNamespacePolicy(SpringConstants.LANG_NAMESPACE_KEY, SpringConstants.LANG_NAMESPACE);
    registerNamespacePolicy(SpringConstants.TOOL_NAMESPACE_KEY, SpringConstants.TOOL_NAMESPACE);
    registerNamespacePolicy(SpringConstants.TX_NAMESPACE_KEY, SpringConstants.TX_NAMESPACE);
    registerNamespacePolicy(SpringConstants.UTIL_NAMESPACE_KEY, SpringConstants.UTIL_NAMESPACE);
    registerNamespacePolicy(SpringConstants.JMS_NAMESPACE_KEY, SpringConstants.JMS_NAMESPACE);
    registerNamespacePolicy(SpringConstants.CONTEXT_NAMESPACE_KEY, SpringConstants.CONTEXT_NAMESPACE);
    registerNamespacePolicy(SpringConstants.P_NAMESPACE_KEY, SpringConstants.P_NAMESPACE);
  }

  public static SpringDomFileDescription getInstance() {
    return EP_NAME.findExtensionOrFail(SpringDomFileDescription.class);
  }
}
