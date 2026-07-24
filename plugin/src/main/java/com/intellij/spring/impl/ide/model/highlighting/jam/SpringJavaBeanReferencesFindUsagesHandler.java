package com.intellij.spring.impl.ide.model.highlighting.jam;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.jam.utils.SpringJamUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.find.FindUsagesHandler;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpringJavaBeanReferencesFindUsagesHandler extends FindUsagesHandler {
  private final DomSpringBean mySpringBean;

  public SpringJavaBeanReferencesFindUsagesHandler(DomSpringBean springBean) {
    super(springBean.getIdentifyingPsiElement());
    mySpringBean = springBean;
  }

  @Override
  @Nonnull
  public PsiElement[] getSecondaryElements() {
    List<SpringJavaBean> list = SpringJamUtils.findBeanReferences(mySpringBean);

    Set<PsiElement> psiElements = new HashSet<PsiElement>();

    for (SpringJavaBean externalBean : list) {
      PsiMethod method = externalBean.getPsiElement();
      if (method != null) {
         psiElements.add(method);
      }
    }
    return psiElements.toArray(new PsiElement[psiElements.size()]);
  }
}
