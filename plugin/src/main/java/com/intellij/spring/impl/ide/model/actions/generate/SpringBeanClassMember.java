package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.editor.generation.ClassMember;
import consulo.language.editor.generation.MemberChooserObject;
import consulo.language.editor.generation.MemberChooserObjectBase;
import consulo.language.editor.generation.PsiElementMemberChooserObject;
import consulo.language.psi.PsiFile;

import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nonnull;

public class SpringBeanClassMember extends MemberChooserObjectBase implements ClassMember {
  private final SpringBeanPointer mySpringBean;

  public SpringBeanClassMember(SpringBeanPointer springBean) {
    super(SpringUtils.getPresentationBeanName(springBean), springBean.getBeanIcon());
    mySpringBean = springBean;
  }

  public MemberChooserObject getParentNodeDelegate() {
    return new SpringFileMemberChooserObjectBase(getSpringBean().getContainingFile());
  }

  public SpringBeanPointer getSpringBean() {
    return mySpringBean;
  }

  private static class SpringFileMemberChooserObjectBase extends PsiElementMemberChooserObject {

    public SpringFileMemberChooserObjectBase(@Nonnull PsiFile psiFile) {
      super(psiFile, psiFile.getName(), psiFile instanceof XmlFile ? SpringIcons.CONFIG_FILE : SpringIcons.JAVA_CONFIG_FILE);
    }

  }
}
