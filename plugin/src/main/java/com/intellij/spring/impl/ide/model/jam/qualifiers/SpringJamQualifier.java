package com.intellij.spring.impl.ide.model.jam.qualifiers;

import com.intellij.jam.JamElement;
import com.intellij.jam.model.common.CommonModelElement;
import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.QualifierAttribute;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.dom.NameValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SpringJamQualifier extends CommonModelElement.PsiBase implements JamElement, SpringQualifier {
  private PsiAnnotation myAnno;
  protected final PsiModifierListOwner myModifierListOwner;
  private CommonSpringBean myQualifiedBean;
  private Project myProject;

  public SpringJamQualifier(@Nonnull PsiAnnotation anno, @Nullable PsiModifierListOwner modifierListOwner, @Nullable CommonSpringBean qualifiedBean) {
    myAnno = anno;
    myModifierListOwner = modifierListOwner;
    myQualifiedBean = qualifiedBean;

    myProject = anno.getProject();
  }

  public PsiAnnotation getAnnotation() {
    return myAnno;
  }

  @Nullable
  @NameValue
  public String getQualifiedName() {
    return JamCommonUtil.getObjectValue(myAnno.findDeclaredAttributeValue(null), String.class)   ;
  }

  @Nonnull
  public PsiModifierListOwner getPsiElement() {
    return myModifierListOwner == null ? getType() : myModifierListOwner;
  }

  @Nonnull
  public PsiClass getType() {
    String annoQualifiedName = myAnno.getQualifiedName();

    return annoQualifiedName == null ? null : JavaPsiFacade.getInstance(myProject).findClass(annoQualifiedName, GlobalSearchScope.allScope(myProject));
  }

  public PsiClass getQualifierType() {
    return getType();
  }

  public String getQualifierValue() {
    return getQualifiedName();
  }

  @Nonnull
  public List<? extends QualifierAttribute> getQualifierAttributes() {
    PsiNameValuePair[] attributes = myAnno.getParameterList().getAttributes();
    ArrayList<QualifierAttribute> list = new ArrayList<QualifierAttribute>();
    for (final PsiNameValuePair pair : attributes) {
      final String name = pair.getName();
      if (name == null || name.equals("value")) {
        continue;
      }
      list.add(new QualifierAttribute() {
        public String getAttributeKey() {
          return name;
        }

        public String getAttributeValue() {
          return JamCommonUtil.getObjectValue(pair.getValue(), String.class);
        }
      });
    }
    return list;
  }

  @Nullable
  public CommonSpringBean getQualifiedBean() {
    return myQualifiedBean;
  }
}
