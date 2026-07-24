/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.psi.*;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.application.util.function.Processor;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author peter
 */
public class PsiBeanPointcutExpression extends AopElementBase implements PsiPointcutExpression {

  public PsiBeanPointcutExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiBeanPointcutExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    return acceptsClass(member instanceof PsiClass ? (PsiClass)member : member.getContainingClass());
  }

  private PointcutMatchDegree acceptsClass(PsiClass psiClass) {
    PsiReference reference = getReference();
    if (reference == null) return PointcutMatchDegree.FALSE;

    Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
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
  public PsiReference getReference() {
    String s = getText();
    final int start = s.indexOf('(');
    if (start < 0) return null;

    int end = s.indexOf(')');
    if (end < 0) end = s.length();
    return new PsiReferenceBase<PsiBeanPointcutExpression>(this, new TextRange(start + 1, end), true) {
      public PsiElement resolve() {
        final Ref<PsiElement> bean = Ref.create(null);
        processBeans(new Processor<SpringBaseBeanPointer>() {
          public boolean process(SpringBaseBeanPointer s) {
            if (getCanonicalText().equals(s.getName())) {
              bean.set(s.getSpringBean().getIdentifyingPsiElement());
              return false;
            }
            return true;
          }
        });
        return bean.get();
      }

      @Override
      public PsiElement handleElementRename(String newText) throws IncorrectOperationException {
        AopPointcutExpressionFile file = (AopPointcutExpressionFile)PsiFileFactory.getInstance(getProject())
                                                                                        .createFileFromText("a",
                                                                                                            AopPointcutExpressionFileType.INSTANCE,
                                                                                                            "bean(" + newText + ")");
        PsiBeanPointcutExpression pointcutExpression = (PsiBeanPointcutExpression)file.getPointcutExpression();
        assert pointcutExpression != null;
        ASTNode parent = getNode().getTreeParent();
        parent.replaceChild(getNode(), pointcutExpression.getNode());
        ASTNode node = parent.findChildByType(getNode().getElementType());
        assert node != null;
        return node.getPsi();
      }

      public Object[] getVariants() {
        final List<LookupElement> result = new ArrayList<LookupElement>();
        processBeans(new Processor<SpringBaseBeanPointer>() {
          public boolean process(SpringBaseBeanPointer bean) {
            String name = bean.getName();
            if (name != null && name.indexOf('#') < 0) {
              result.add(LookupElementBuilder.create(name).withIcon(SpringIcons.SPRING_BEAN_ICON));
            }
            return true;
          }
        });
        return result.toArray();
      }
    };
  }

  private boolean processBeans(Processor<SpringBaseBeanPointer> processor) {
    AopAdvisedElementsSearcher searcher = getContainingFile().getAopModel().getAdvisedElementsSearcher();
    if (!(searcher instanceof SpringAdvisedElementsSearcher)) return true;
    for (SpringModel model : ((SpringAdvisedElementsSearcher)searcher).getSpringModels()) {
      for (SpringBaseBeanPointer pointer : model.getAllCommonBeans(true)) {
        if (!processor.process(pointer)) return false;

      }
    }
    return true;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }

}