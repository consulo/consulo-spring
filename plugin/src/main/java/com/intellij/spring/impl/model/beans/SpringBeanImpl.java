/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.ResolvedConstructorArgs;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import com.intellij.spring.impl.model.AbstractDomSpringBean;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringBeanImpl extends DomSpringBeanImpl implements SpringBean {

  private CachedValue<ResolvedConstructorArgsImpl> myResolvedConstructorArgs;
  public static final Function<SpringBean, Collection<SpringPropertyDefinition>> PROPERTIES_GETTER =
    SpringUtils::getProperties;
  public static final Function<SpringBean, Collection<ConstructorArg>> CTOR_ARGS_GETTER =
    SpringUtils::getConstructorArgs;

  /**
   * Returns bean name (id or the first name from "name" attribute).
   *
   * @return bean name (id or the first name from "name" attribute).
   */
  @Nullable
  public String getBeanName() {
    String id = getId().getStringValue();
    if (id != null) {
      return id;
    }
    else {
      String name = getName().getStringValue();
      if (name != null) {
        List<String> list = SpringUtils.tokenize(name);
        return list.size() > 0 ? list.get(0) : null;
      }
    }
    return getClazz().getStringValue();
  }

  @Nullable
  public String getClassName() {
    return getClazz().getStringValue();
  }

  @Nonnull
  public String[] getAliases() {
    String name = getName().getStringValue();
    if (name != null) {
      List<String> list = SpringUtils.tokenize(name);
      String id = getId().getStringValue();
      if (id == null && list.size() > 1) {
        list.remove(0);
      }
      return ArrayUtil.toStringArray(list);
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public void setName(@Nonnull String newName) {
    String id = getId().getStringValue();
    if (id != null) {
      getId().setStringValue(newName);
    }
    else {
      String name = getName().getStringValue();
      if (name != null) {
        int first = StringUtil.findFirst(name, SpringUtils.ourFilter);
        if (first >= 0) {
          String newValue = newName + name.substring(first);
          getName().setStringValue(newValue);
        }
        else {
          getName().setStringValue(newName);
        }
      }
      else {
        getId().setValue(newName);
      }
    }
  }

  @Nonnull
  public abstract GenericAttributeValue<SpringBeanPointer> getFactoryBean();

  @Nonnull
  public abstract GenericAttributeValue<PsiMethod> getFactoryMethod();

  @Nullable
  public PsiClass getBeanClass(@Nullable Set<AbstractDomSpringBean> visited, boolean considerFactories) {
    if (visited != null && visited.contains(this)) return null;

    PsiClass psiClass = super.getBeanClass(visited, considerFactories);
    if (psiClass != null) return psiClass;

    SpringBeanPointer parent = getParentBean().getValue();
    if (parent != null) {
      if (visited == null) {
        visited = new HashSet<AbstractDomSpringBean>();
      }
      visited.add(this);
      CommonSpringBean bean = parent.getSpringBean();
      if (bean instanceof DomSpringBeanImpl) {
        return ((DomSpringBeanImpl)bean).getBeanClass(visited, considerFactories);
      }
    }
    return null;
  }

  public List<SpringPropertyDefinition> getAllProperties() {
    Set<SpringPropertyDefinition> list = SpringUtils.getMergedSet(this, PROPERTIES_GETTER);
    return new ArrayList<SpringPropertyDefinition>(list);
  }

  public Set<ConstructorArg> getAllConstructorArgs() {
    return SpringUtils.getMergedSet(this, CTOR_ARGS_GETTER);
  }

  @Nonnull
  public ResolvedConstructorArgs getResolvedConstructorArgs() {
    if (myResolvedConstructorArgs == null) {
      CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(getManager().getProject());
      myResolvedConstructorArgs = cachedValuesManager.createCachedValue(new CachedValueProvider<ResolvedConstructorArgsImpl>() {
        public Result<ResolvedConstructorArgsImpl> compute() {
          return Result.createSingleDependency(new ResolvedConstructorArgsImpl(SpringBeanImpl.this),
                                               PsiModificationTracker.MODIFICATION_COUNT);
        }
      }, false);
    }
    ResolvedConstructorArgs resolvedConstructorArgs = myResolvedConstructorArgs.getValue();
    assert resolvedConstructorArgs != null;
    return resolvedConstructorArgs;
  }

  public boolean isAbstract() {
    Boolean value = getAbstract().getValue();
    return value != null && value.booleanValue();
  }

  public Autowire getBeanAutowire() {
    Autowire autowire = getAutowire().getValue();
    if (autowire == null) {
      Beans beans = getParentOfType(Beans.class, false);
      assert beans != null;
      autowire = Autowire.fromDefault(beans.getDefaultAutowire().getValue());
    }
    if (autowire == Autowire.AUTODETECT) {
      return SpringConstructorArgResolveUtil.hasEmptyConstructor(this) ? Autowire.BY_TYPE : Autowire.CONSTRUCTOR;
    }
    else {
      return autowire;
    }
  }

  @NonNls
  public String toString() {
    String beanName = getBeanName();
    return beanName == null ? "Unknown" : beanName;
  }

  public SpringQualifier getSpringQualifier() {
    SpringDomQualifier qualifier = getQualifier();
    return !DomUtil.hasXml(qualifier) ? null : qualifier;
  }
}
