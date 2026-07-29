/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.psi.*;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.module.Module;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.util.lang.StringUtil;

import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author peter
 */
public class PsiBeanPointcutExpression extends AopElementBase implements PsiPointcutExpression {
  public PsiBeanPointcutExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "PsiBeanPointcutExpression";
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    return acceptsClass(member instanceof PsiClass psiClass ? psiClass : member.getContainingClass());
  }

  @RequiredReadAction
  private PointcutMatchDegree acceptsClass(PsiClass psiClass) {
    PsiReference reference = getReference();
    if (reference == null) return PointcutMatchDegree.FALSE;

    Module module = psiClass.getModule();
    if (module == null) return PointcutMatchDegree.FALSE;

    Pattern pattern = Pattern.compile(reference.getCanonicalText().replaceAll(" ", "").replaceAll("\\*", "\\.\\*"));

    for (SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
      for (SpringBaseBeanPointer pointer : model.findBeansByPsiClass(psiClass)) {
        String name = pointer.getName();
        if (StringUtil.isNotEmpty(name) && pattern.matcher(name).matches()) {
          return PointcutMatchDegree.TRUE;
        }
      }
    }

    return PointcutMatchDegree.FALSE;
  }

  @Override
  @RequiredReadAction
  public PsiReference getReference() {
    String s = getText();
    final int start = s.indexOf('(');
    if (start < 0) return null;

    int end = s.indexOf(')');
    if (end < 0) end = s.length();
    return new PsiReferenceBase<>(this, new TextRange(start + 1, end), true) {
      @Override
      @RequiredReadAction
      public PsiElement resolve() {
        SimpleReference<PsiElement> bean = SimpleReference.create(null);
        processBeans(s1 -> {
          if (getCanonicalText().equals(s1.getName())) {
            bean.set(s1.getSpringBean().getIdentifyingPsiElement());
            return false;
          }
          return true;
        });
        return bean.get();
      }

      @Override
      @RequiredWriteAction
      public PsiElement handleElementRename(String newText) throws IncorrectOperationException {
        AopPointcutExpressionFile file = (AopPointcutExpressionFile)PsiFileFactory.getInstance(getProject())
          .createFileFromText("a", AopPointcutExpressionFileType.INSTANCE, "bean(" + newText + ")");
        PsiBeanPointcutExpression pointcutExpression = (PsiBeanPointcutExpression)file.getPointcutExpression();
        assert pointcutExpression != null;
        ASTNode parent = getNode().getTreeParent();
        parent.replaceChild(getNode(), pointcutExpression.getNode());
        ASTNode node = parent.findChildByType(getNode().getElementType());
        assert node != null;
        return node.getPsi();
      }

      @Override
      @RequiredReadAction
      public Object[] getVariants() {
        List<LookupElement> result = new ArrayList<>();
        processBeans(bean -> {
          String name = bean.getName();
          if (name != null && name.indexOf('#') < 0) {
            result.add(LookupElementBuilder.create(name).withIcon(SpringImplIconGroup.springbean()));
          }
          return true;
        });
        return result.toArray();
      }
    };
  }

  private boolean processBeans(Predicate<SpringBaseBeanPointer> processor) {
    AopAdvisedElementsSearcher searcher = getContainingFile().getAopModel().getAdvisedElementsSearcher();
    if (!(searcher instanceof SpringAdvisedElementsSearcher advisedElementsSearcher)) return true;
    for (SpringModel model : advisedElementsSearcher.getSpringModels()) {
      for (SpringBaseBeanPointer pointer : model.getAllCommonBeans(true)) {
        if (!processor.test(pointer)) return false;
      }
    }
    return true;
  }

  @Nonnull
  @Override
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }
}