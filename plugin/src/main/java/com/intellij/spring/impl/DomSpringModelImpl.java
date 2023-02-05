/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.impl.ide.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.impl.ide.model.jam.utils.SpringJamUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.application.util.AtomicNotNullLazyValue;
import consulo.application.util.ConcurrentFactoryMap;
import consulo.module.Module;
import consulo.spring.impl.DomSpringModel;
import consulo.util.collection.MultiMap;
import consulo.util.collection.SmartList;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.util.xml.DomFileElement;
import consulo.xml.util.xml.DomUtil;
import consulo.xml.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Dmitry Avdeev
 */
public class DomSpringModelImpl extends DomModelImpl<Beans> implements DomSpringModel {

  private abstract static class Class2BeansMap {
    private final Map<String, List<SpringBaseBeanPointer>> myMap = new HashMap<String, List<SpringBaseBeanPointer>>();

    List<SpringBaseBeanPointer> get(PsiClass psiClass) {
      String fqn = psiClass.getQualifiedName();
      if (fqn == null) {
        return Collections.emptyList();
      }
      List<SpringBaseBeanPointer> pointers = myMap.get(fqn);
      if (pointers == null) {
        pointers = new ArrayList<SpringBaseBeanPointer>();
        compute(psiClass, pointers);
        myMap.put(fqn, pointers);
      }
      return pointers;

    }

    protected abstract void compute(PsiClass psiClass, List<SpringBaseBeanPointer> pointers);
  }

  private final Map<SpringQualifier, List<SpringBaseBeanPointer>> myBeansByQualifier =
    ConcurrentFactoryMap.createMap(this::computeBeansByQualifier);

  private final Map<PsiClass, List<SpringBaseBeanPointer>> myBeansByClass =
    ConcurrentFactoryMap.createMap(key -> computeBeansByPsiClass(key));

  private final Class2BeansMap myBeansByEffectiveClassWithInheritance = new Class2BeansMap() {
    @Override
    protected void compute(PsiClass psiClass, List<SpringBaseBeanPointer> pointers) {
      for (final SpringBaseBeanPointer bean : getAllCommonBeans()) {
        for (PsiClass beanClass : bean.getEffectiveBeanType()) {
          if (InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
            pointers.add(bean);
          }
        }
      }
    }
  };

  private final AtomicNotNullLazyValue<MultiMap<PsiClass, SpringBaseBeanPointer>>
    myBeansByClassWithInheritance =
    new AtomicNotNullLazyValue<MultiMap<PsiClass, SpringBaseBeanPointer>>() {
      @Nonnull
      @Override
      protected MultiMap<PsiClass, SpringBaseBeanPointer> compute() {
        return computeBeansByPsiClassWithInheritance();
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<String, XmlTag>> myCustomBeanIdCandidates =
    new AtomicNotNullLazyValue<MultiMap<String, XmlTag>>() {
      @Nonnull
      @Override
      protected MultiMap<String, XmlTag> compute() {
        final MultiMap<String, XmlTag> map = new MultiMap<String, XmlTag>();
        for (final DomFileElement<Beans> element : getRoots()) {
          for (CustomBeanWrapper bean : DomUtil.getDefinedChildrenOfType(element.getRootElement(), CustomBeanWrapper.class)) {
            if (!bean.isParsed()) {
              final XmlTag tag = bean.getXmlTag();
              for (XmlAttribute attribute : tag.getAttributes()) {
                map.putValue(attribute.getDisplayValue(), tag);
              }
            }
          }
        }

        return map;
      }
    };

  private BeanNamesMapper myBeanNamesMapper;

  @Nullable
  private final Module myModule;

  private SpringModel[] myDependencies = EMPTY_ARRAY;

  private final SpringFileSet myFileSet;

  private Collection<? extends SpringBaseBeanPointer> myBeansWithoutDependencies;

  private Collection<? extends SpringBaseBeanPointer> myBeans;

  private final AtomicNotNullLazyValue<Collection<SpringBaseBeanPointer>> myOwnBeans =
    new AtomicNotNullLazyValue<Collection<SpringBaseBeanPointer>>() {
      @Nonnull
      protected Collection<SpringBaseBeanPointer> compute() {
        Collection<SpringBaseBeanPointer> beans = null;
        for (final DomFileElement<Beans> element : getRoots()) {
          final List<CommonSpringBean> springBeanList = SpringUtils.getChildBeans(element.getRootElement(), false);
          if (beans == null) {
            beans = new ArrayList<SpringBaseBeanPointer>(springBeanList.size());
          }
          for (CommonSpringBean bean : springBeanList) {
            beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
          }
        }
        return beans == null ? Collections.<SpringBaseBeanPointer>emptySet() : beans;
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>> myDirectInheritorsMap =
    new AtomicNotNullLazyValue<MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>>() {
      @Nonnull
      protected MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> compute() {
        final MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = new MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>() {
          protected Map<SpringBaseBeanPointer, Collection<SpringBaseBeanPointer>> createMap() {
            return new ConcurrentHashMap<SpringBaseBeanPointer, Collection<SpringBaseBeanPointer>>();
          }
        };
        for (final SpringBaseBeanPointer pointer : getAllDomBeans()) {
          final SpringBeanPointer parentPointer = pointer.getParentPointer();
          if (parentPointer != null) {
            map.putValue(parentPointer.getBasePointer(), pointer);
          }
        }
        return map;
      }
    };

  private List<SpringJavaConfiguration> myJavaConfigurations;

  private interface ModelVisitor {


    /**
     * @param model
     * @return false to stop traversing
     */
    boolean visit(SpringModel model);

  }

  public DomSpringModelImpl(@Nonnull final DomFileElement<Beans> mergedModel,
                            @Nonnull final Set<XmlFile> configFiles,
                            final consulo.module.Module module,
                            final SpringFileSet fileSet) {

    super(mergedModel, configFiles);
    myFileSet = fileSet;
    myModule = module;
  }

  public List<SpringJavaConfiguration> getJavaConfigurations() {

    return SpringJamUtils.getJavaConfigurations(this);
  }

  private boolean visitDependencies(final ModelVisitor visitor) {
    for (SpringModel dependency : myDependencies) {
      if (!visitor.visit(dependency)) {
        return false;
      }
      if (dependency instanceof DomSpringModelImpl) {
        ((DomSpringModelImpl)dependency).visitDependencies(visitor);
      }
    }
    return true;
  }

  List<Alias> getAliases(boolean withDeps) {
    final ArrayList<Alias> list = new ArrayList<Alias>();
    final ModelVisitor modelVisitor = new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        for (DomFileElement<Beans> fileElement : model.getRoots()) {
          list.addAll(fileElement.getRootElement().getAliases());
        }
        return true;
      }
    };
    modelVisitor.visit(this);
    if (withDeps) {
      visitDependencies(modelVisitor);
    }
    return list;
  }

  @Nonnull
  public String getId() {
    return myFileSet.getId();
  }

  public SpringFileSet getFileSet() {
    return myFileSet;
  }

  @Nonnull
  public SpringModel[] getDependencies() {
    return myDependencies == null ? EMPTY_ARRAY : myDependencies;
  }

  @Nonnull
  public Collection<XmlTag> getCustomBeanCandidates(String id) {
    return myCustomBeanIdCandidates.getValue().get(id);
  }

  public void setDependencies(@Nonnull final SpringModel[] dependencies) {
    myDependencies = dependencies;
  }

  @Nullable
  public SpringBeanPointer findBean(@NonNls @Nonnull String beanName) {
    return getBeanNamesMapper().getBean(beanName);
  }

  @Nullable
  public SpringBeanPointer findParentBean(@NonNls @Nonnull final String beanName) {
    for (SpringModel dependency : myDependencies) {
      final SpringBeanPointer springBean = dependency.findBean(beanName);
      if (springBean != null) {
        return springBean;
      }
    }
    return null;
  }

  private BeanNamesMapper getBeanNamesMapper() {
    if (myBeanNamesMapper == null) {
      myBeanNamesMapper = new BeanNamesMapper(this);
    }
    return myBeanNamesMapper;
  }

  @Nonnull
  public Collection<SpringBaseBeanPointer> getAllDomBeans() {
    return getAllDomBeans(true);
  }

  @Nonnull
  public Collection<SpringBaseBeanPointer> getOwnBeans() {
    return myOwnBeans.getValue();
  }

  @Nonnull
  public Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDependencies) {

    final Collection<SpringBaseBeanPointer> ownBeans = getOwnBeans();
    if (withDependencies) {
      final List<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>(ownBeans);
      visitDependencies(new ModelVisitor() {
        public boolean visit(final SpringModel model) {
          allBeans.addAll(model.getOwnBeans());
          return true;
        }
      });
      return allBeans;
    }
    else {
      return ownBeans;
    }
  }

  @Nonnull
  public Set<String> getAllBeanNames(@Nonnull final String beanName) {
    return getBeanNamesMapper().getAllBeanNames(beanName);
  }

  public boolean isNameDuplicated(@Nonnull final String beanName) {
    return getBeanNamesMapper().isNameDuplicated(beanName);
  }

  @Nonnull
  public synchronized Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(final boolean withDepenedencies) {
    if (!withDepenedencies || myDependencies.length == 0) {
      return myBeansWithoutDependencies == null
        ? myBeansWithoutDependencies = calculateBeans(withDepenedencies)
        : myBeansWithoutDependencies;
    }
    else {
      return myBeans == null ? myBeans = calculateBeans(withDepenedencies) : myBeans;
    }
  }

  private Collection<SpringBaseBeanPointer> calculateBeans(final boolean withDepenedencies) {
    Collection<SpringBaseBeanPointer> domBeans = getAllDomBeans(withDepenedencies);
    final Collection<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>(domBeans);

    processNonDomBeans(bean -> allBeans.add(SpringBeanPointer.createSpringBeanPointer(bean)));

    return allBeans;
  }

  private void processNonDomBeans(final Consumer<CommonSpringBean> consumer) {
    for (SpringJavaConfiguration javaConfiguration : getJavaConfigurations()) {
      for (SpringJavaBean javaBean : javaConfiguration.getBeans()) {
        if (javaBean.isPublic()) {
          consumer.accept(javaBean);
        }
      }
    }
    for (final SpringStereotypeElement element : SpringJamUtils.getAllStereotypeJavaBeans(this)) {
      consumer.accept(element);
    }
  }

  @Nonnull
  public Collection<? extends SpringBaseBeanPointer> getAllCommonBeans() {
    return getAllCommonBeans(true);
  }

  @Nonnull
  public Collection<? extends SpringBaseBeanPointer> getAllParentBeans() {
    final Collection<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>();

    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        allBeans.addAll(model.getAllCommonBeans());
        return true;
      }
    });

    return allBeans;
  }

  @Nonnull
  public List<SpringBaseBeanPointer> findQualifiedBeans(@Nonnull final SpringQualifier qualifier) {
    final List<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(myBeansByQualifier.get(qualifier));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((DomSpringModelImpl)model).myBeansByQualifier.get(qualifier));
        return true;
      }
    });
    return pointers;
  }

  private List<SpringBaseBeanPointer> computeBeansByQualifier(final SpringQualifier pair) {
    final List<SpringBaseBeanPointer> beans = new ArrayList<SpringBaseBeanPointer>();
    final Collection<? extends SpringBaseBeanPointer> pointers = getAllCommonBeans(true);
    for (SpringBaseBeanPointer beanPointer : pointers) {
      final CommonSpringBean bean = beanPointer.getSpringBean();
      final SpringQualifier qualifier = bean.getSpringQualifier();
      if (qualifier != null) {
        if (SpringUtils.compareQualifiers(qualifier, pair)) {
          beans.add(beanPointer);
        }
      }
    }
    return beans;
  }


  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByPsiClass(@Nonnull final PsiClass psiClass) {
    final List<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(myBeansByClass.get(psiClass));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((DomSpringModelImpl)model).myBeansByClass.get(psiClass));
        return true;
      }
    });
    return pointers;
  }

  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@Nonnull final PsiClass psiClass) {
    final ArrayList<SpringBaseBeanPointer> pointers =
      new ArrayList<SpringBaseBeanPointer>(myBeansByClassWithInheritance.getValue().get(psiClass));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((DomSpringModelImpl)model).myBeansByClassWithInheritance.getValue().get(psiClass));
        return true;
      }
    });

    return pointers;
  }

  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@Nonnull final PsiClass psiClass) {
    return collectBeans(psiClass, springModel -> springModel.myBeansByEffectiveClassWithInheritance);
  }

  private List<SpringBaseBeanPointer> collectBeans(final PsiClass psiClass, final Function<DomSpringModelImpl, Class2BeansMap> getter) {
    final ArrayList<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(getter.apply(this).get(psiClass));
    visitDependencies(model -> {
      pointers.addAll(getter.apply((DomSpringModelImpl)model).get(psiClass));
      return true;
    });

    return pointers;
  }

  @Nonnull
  public List<SpringBaseBeanPointer> getChildren(@Nonnull SpringBeanPointer parent) {
    final SpringBaseBeanPointer baseParent = parent.getBasePointer();
    final ArrayList<SpringBaseBeanPointer> list = new ArrayList<SpringBaseBeanPointer>();
    for (SpringBaseBeanPointer bean : getAllDomBeans()) {
      final SpringBeanPointer pointer = bean.getParentPointer();
      if (pointer != null && pointer.getBasePointer().equals(baseParent)) {
        list.add(bean);
      }
    }
    return list;
  }

  private static void addDescendants(MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map,
                                     SpringBaseBeanPointer current,
                                     Set<SpringBaseBeanPointer> result) {
    final Collection<SpringBaseBeanPointer> pointers = map.get(current);
    for (final SpringBaseBeanPointer pointer : pointers) {
      if (result.add(pointer)) {
        addDescendants(map, pointer, result);
      }
    }
  }

  @Nonnull
  public List<SpringBaseBeanPointer> getDescendants(final @Nonnull CommonSpringBean context) {
    final Set<SpringBaseBeanPointer> visited = new HashSet<SpringBaseBeanPointer>();
    final SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(context);
    visited.add(pointer);
    final MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = myDirectInheritorsMap.getValue();
    addDescendants(map, pointer, visited);
    return new SmartList<SpringBaseBeanPointer>(visited);
  }

  private List<SpringBaseBeanPointer> computeBeansByPsiClass(@Nonnull final PsiClass psiClass) {
    final List<SpringBaseBeanPointer> beans = new ArrayList<SpringBaseBeanPointer>();
    final Consumer<CommonSpringBean> consumer = bean -> {
      final PsiClass beanClass = bean.getBeanClass();
      if (beanClass != null && beanClass.equals(psiClass)) {
        beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
      }
    };

    processAllBeans(consumer);

    return beans;
  }

  private void processAllBeans(final Consumer<CommonSpringBean> consumer) {
    final SpringModelVisitor visitor = new SpringModelVisitor() {
      public boolean visitBean(final CommonSpringBean bean) {
        consumer.accept(bean);
        return true;
      }
    };
    for (final DomFileElement<Beans> element : getRoots()) {
      SpringModelVisitor.visitBeans(visitor, element.getRootElement());
    }
    processNonDomBeans(consumer);
  }

  private MultiMap<PsiClass, SpringBaseBeanPointer> computeBeansByPsiClassWithInheritance() {
    final MultiMap<PsiClass, SpringBaseBeanPointer> result = new MultiMap<PsiClass, SpringBaseBeanPointer>();
    final Consumer<CommonSpringBean> consumer = new Consumer<CommonSpringBean>() {
      public void accept(final CommonSpringBean bean) {
        final PsiClass beanClass = bean.getBeanClass();
        if (beanClass == null) {
          return;
        }

        final SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(bean);
        InheritanceUtil.processSupers(beanClass, true, psiClass -> {
          result.putValue(psiClass, pointer);
          return true;
        });
      }
    };

    processAllBeans(consumer);

    return result;
  }

  @Nullable
  public Module getModule() {
    return myModule;
  }

  public String toString() {
    return getId();
  }
}
