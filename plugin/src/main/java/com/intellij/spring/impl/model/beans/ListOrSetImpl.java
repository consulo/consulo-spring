package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiArrayType;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import com.intellij.spring.impl.ide.model.xml.beans.TypeHolder;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.dom.DomElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ListOrSetImpl extends TypedCollectionImpl implements ListOrSet {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    List<? extends PsiType> list = super.getRequiredTypes();
    if (!list.isEmpty()) {
      return list;
    }
    PsiType fromGenerics = getRequiredTypeFromGenerics();
    Project project = getManager().getProject();

    PsiType type = fromGenerics != null
                         ? fromGenerics
                         : PsiType.getJavaLangObject(PsiManager.getInstance(project), GlobalSearchScope.allScope(project));
    return Collections.singletonList(type);  
  }

  @Nullable
  private PsiType getRequiredTypeFromGenerics() {
    DomElement parent = getParent();
    if (parent instanceof TypeHolder) {
      List<? extends PsiType> types = ((TypeHolder)parent).getRequiredTypes();
      if (types.isEmpty()) return null;
      PsiType type = types.get(0);
      if (type instanceof PsiClassType) {
        List<PsiType> list = SpringUtils.resolveGenerics((PsiClassType)type);

        return list.size() == 1 ? list.get(0) : null;
      } else if (type instanceof PsiArrayType) {
        return ((PsiArrayType)type).getComponentType();
      }
    }
    return null;
  }
}
