package com.intellij.spring.impl.ide.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.model.common.CommonModelElement;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class JamPsiMemberSpringBean<T extends PsiMember> extends CommonModelElement.PsiBase
  implements JamElement, CommonSpringBean {

  @Nonnull
  @JamPsiConnector
  public abstract T getPsiElement();

  @Nullable
  public PsiClass getBeanClass(boolean considerFactories) {
    return getBeanClass();
  }

  @Nonnull
  public String[] getAliases() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  protected List<String> getStringNames(List<JamStringAttributeElement<String>> elements) {
    List<String> aliases = new ArrayList<String>();
    for (JamStringAttributeElement<String> element : elements) {
      String aliasName = element.getStringValue();
      if (!StringUtil.isEmptyOrSpaces(aliasName)) {
        aliases.add(aliasName);
      }
    }
    return aliases;
  }

  public SpringQualifier getSpringQualifier() {
    return null;
  }
}
