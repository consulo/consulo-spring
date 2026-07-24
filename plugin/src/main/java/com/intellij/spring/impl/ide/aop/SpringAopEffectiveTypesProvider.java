package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.SpringBeanEffectiveTypeProvider;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import java.util.*;

@ExtensionImpl
public class SpringAopEffectiveTypesProvider extends SpringBeanEffectiveTypeProvider {


  public void processEffectiveTypes(@Nonnull CommonSpringBean bean, Collection<PsiClass> result) {
    Set<PsiClass> toAdd = new HashSet<PsiClass>();
    List<PsiClass> toRemove = new ArrayList<>();
    for (PsiClass psiClass : result) {
      for (AopIntroduction introduction : AopJavaAnnotator.getBoundIntroductions(psiClass)) {
        ContainerUtil.addIfNotNull(toAdd, introduction.getImplementInterface().getValue());
      }
   }
    result.addAll(toAdd);
    result.removeAll(toRemove);
  }


  
}