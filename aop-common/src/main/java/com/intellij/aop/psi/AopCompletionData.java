/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.lexer.AopLexer;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiKeyword;
import com.intellij.java.language.psi.PsiModifier;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.*;
import consulo.language.pattern.ElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.ProcessingContext;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

import static consulo.language.pattern.PlatformPatterns.psiElement;
import static consulo.language.pattern.StandardPatterns.*;

/**
 * @author peter
 */
@ExtensionImpl
public class AopCompletionData extends CompletionContributor implements AopElementTypes {
  private static final Key<PsiPointcutReferenceExpression> POINTCUT_REFERENCE_KEY = Key.create("POINTCUT_REFERENCE_KEY");

  private static final ElementPattern<PsiElement> AFTER_AT_FILTER = psiElement().afterLeaf("@").inFile(
    psiElement(AopPointcutExpressionFile.class));
  private static final ElementPattern<PsiElement> POINTCUT_REFERENCE_PATTERN = psiElement()
    .withParent(psiElement(AopReferenceExpression.class).inside(psiElement(PsiPointcutReferenceExpression.class).save(POINTCUT_REFERENCE_KEY)))
    .and(not(psiElement().inside(psiElement(AopReferenceHolder.class))))
    .and(not(AFTER_AT_FILTER));
  public static final ElementPattern<PsiElement> POINTCUT_PATTERN = or(
    psiElement().withParent(
      psiElement(AopReferenceExpression.class).withoutText(".").withParent(
        psiElement(PsiPointcutReferenceExpression.class))
    ), AFTER_AT_FILTER);

  public AopCompletionData() {
    extend(CompletionType.BASIC, POINTCUT_PATTERN, (parameters, context, _result) -> {
      CompletionResultSet result =
        _result.withPrefixMatcher(findPrefix(parameters.getPosition(), parameters.getOffset(), _result.getPrefixMatcher().getPrefix()));
      result.stopHere();
      for (String string : getAllPointcutDesignators()) {
        result.addElement(createPointcutDesignatorElement(string));
      }
      PsiReference reference = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      if (reference instanceof AopReferenceExpression expression) {
        for (LookupElement element : expression.getVariants()) {
          result.addElement(element);
        }
      }
    });

    String[] modifiers = {PsiModifier.PUBLIC, PsiModifier.PRIVATE, PsiModifier.PROTECTED};
    extend(
      CompletionType.BASIC,
      modifier(not(string().oneOf(modifiers))),
      (parameters, context, result) -> {
        for (final String modifier : modifiers) {
          result.addElement(keyword(modifier));
        }
      }
    );

    extend(
      CompletionType.BASIC,
      modifier(not(string().equalTo(PsiModifier.SYNCHRONIZED))),
      (parameters, context, result) -> result.addElement(keyword(PsiModifier.STATIC))
    );
    extend(
      CompletionType.BASIC,
      modifier(not(string().equalTo(PsiModifier.STATIC))),
      (parameters, context, result) -> result.addElement(keyword(PsiModifier.SYNCHRONIZED))
    );
    extend(
      CompletionType.BASIC,
      modifier(string()),
      (parameters, context, result) -> result.addElement(keyword(PsiModifier.FINAL))
    );
    extend(
      CompletionType.BASIC,
      psiElement().afterLeafSkipping(
        psiElement().whitespace(),
        psiElement(AOP_RIGHT_PAR).afterLeafSkipping(
          not(psiElement(AOP_LEFT_PAR)),
          psiElement(AOP_LEFT_PAR).afterLeaf(psiElement().inside(psiElement(AopMemberReferenceExpression.class)))
        )
      ),
      (parameters, context, result) -> result.addElement(keyword(PsiKeyword.THROWS))
    );

    Set<String> primitiveTypes = new HashSet<>(AopLexer.PRIMITIVE_TYPES.keySet());
    primitiveTypes.remove("void");
    extend(
      CompletionType.BASIC,
      psiElement().withParent(psiElement(AopReferenceExpression.class))
        .and(not(psiElement().afterLeaf(psiElement().withElementType(AOP_DOTS))))
        .and(not(
          psiElement().inside(or(
            psiElement(AopMemberReferenceExpression.class),
            psiElement(PsiAtPointcutDesignator.class),
            psiElement(PsiTargetExpression.class),
            psiElement(PsiThisExpression.class),
            psiElement(PsiWithinExpression.class),
            psiElement(PsiPointcutReferenceExpression.class))
          )
        )),
      (parameters, context, result) -> {
        for (String primitiveType : primitiveTypes) {
          result.addElement(keyword(primitiveType));
        }
      }
    );

    extend(
      CompletionType.BASIC,
      psiElement().withParent(psiElement(AopReferenceExpression.class))
        .afterLeafSkipping(
          or(psiElement().whitespace(), psiElement().withElementType(AOP_MODIFIER), psiElement(AOP_NOT)),
          psiElement(AOP_LEFT_PAR).withParent(PsiExecutionExpression.class)
        ),
        (parameters, context, result) -> result.addElement(keyword("void"))
    );

    extend(
      CompletionType.CLASS_NAME,
      psiElement().inside(psiElement(PsiAtPointcutDesignator.class)),
      (parameters, context, result) -> result.runRemainingContributors(parameters, completionResult -> {
        LookupElement lookupElement = completionResult.getLookupElement();
        Object o = lookupElement.getObject();
        if (o instanceof PsiClass psiClass && psiClass.isAnnotationType()) {
          result.addElement(lookupElement);
        }
      })
    );
  }

  public static Set<String> getAllPointcutDesignators() {
    return AopPointcutTypes.getPointcutTokens().keySet();
  }

  private static LookupElement keyword(String keyword) {
    return TailTypeDecorator.withTail(LookupElementBuilder.create(keyword).setBold(), TailType.SPACE);
  }

  public static LookupElement createPointcutDesignatorElement(String s) {
    return LookupElementBuilder.create(s).setInsertHandler(ParenthesesInsertHandler.WITH_PARAMETERS).setBold();
  }

  private static ElementPattern<PsiElement> modifier(final ElementPattern allowedModifiers) {
    return psiElement().withSuperParent(2, psiElement(AopReferenceHolder.class).withParent(
      psiElement(PsiExecutionExpression.class))).afterLeafSkipping(
      or(
        psiElement().whitespace(),
        psiElement(AOP_NOT),
        psiElement().withElementType(AOP_MODIFIER).withText(allowedModifiers)
      ),
      psiElement().withText("(")
    );
  }

  @RequiredReadAction
  private static String findPrefix(final PsiElement insertedElement, final int offsetInFile, String oldPrefix) {
    final ProcessingContext matchingContext = new ProcessingContext();
    if (POINTCUT_REFERENCE_PATTERN.accepts(insertedElement, matchingContext)) {
      final PsiElement psiElement = matchingContext.get(POINTCUT_REFERENCE_KEY);
      return psiElement.getText().substring(0, offsetInFile - psiElement.getTextRange().getStartOffset());
    }

    return AFTER_AT_FILTER.accepts(insertedElement) ? "@" + oldPrefix : oldPrefix;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return AopPointcutExpressionLanguage.INSTANCE;
  }
}
