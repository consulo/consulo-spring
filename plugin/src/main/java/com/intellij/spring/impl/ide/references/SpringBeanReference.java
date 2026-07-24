package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.EmptyResolveMessageProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpringBeanReference extends PsiReferenceBase<PsiLiteralExpression> implements EmptyResolveMessageProvider {
  private final PsiClass myRequiredClass;

  public SpringBeanReference(PsiLiteralExpression element, PsiClass requiredClass) {
    super(element);
    myRequiredClass = requiredClass;
  }

  public SpringBeanReference(PsiLiteralExpression element) {
    this(element, null);
  }

  public PsiElement resolve() {
    Object value = myElement.getValue();
    if (!(value instanceof String)) return null;

    SpringModel model = getSpringModel();
    if (model == null) return null;
    SpringBeanPointer springBean = model.findBean((String)value);

    return springBean == null ? null : springBean.getPsiElement();
  }

  @Override
  public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
  {
    return getElement();
  }

  @Nullable
  @RequiredReadAction
  private SpringModel getSpringModel() {
    Module module = myElement.getModule();
    if (module == null) return null;

    return SpringManager.getInstance(module.getProject()).getModel(module);
  }

  @Override
  public Object[] getVariants() {
    List<Object> lookups = new ArrayList<Object>();
    SpringModel model = getSpringModel();
    if (model != null) {
      Collection<? extends SpringBeanPointer> list = model.getAllCommonBeans(true);

      for (SpringBeanPointer bean : list) {
        String beanName = bean.getName();
        if (beanName != null && StringUtil.isNotEmpty(beanName)) {
          PsiClass beanClass = bean.getBeanClass();
          if (myRequiredClass != null && (beanClass == null || !InheritanceUtil.isInheritorOrSelf(beanClass, myRequiredClass, true))) {
            continue;            
          }
          lookups.add(SpringBeanResolveConverter.createCompletionVariant(bean));
        }
      }
    }
    return ArrayUtil.toObjectArray(lookups);
  }

  @Nonnull
  @Override
  public LocalizeValue buildUnresolvedMessage(@Nonnull String s) {
    return LocalizeValue.localizeTODO(SpringBundle.message("model.bean.error.message", getValue()));
  }
}
