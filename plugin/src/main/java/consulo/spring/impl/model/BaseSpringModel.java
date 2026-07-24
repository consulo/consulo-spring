package consulo.spring.impl.model;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.BeanNamesMapper;
import com.intellij.spring.impl.Class2BeansMap;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.jam.utils.SpringJamUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.application.util.AtomicNotNullLazyValue;
import consulo.application.util.ConcurrentFactoryMap;
import consulo.module.Module;
import consulo.util.collection.MultiMap;
import consulo.util.collection.SmartList;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2024-04-13
 */
public abstract class BaseSpringModel implements SpringModel {
  private final Map<SpringQualifier, List<SpringBaseBeanPointer>> myBeansByQualifier =
    ConcurrentFactoryMap.createMap(this::computeBeansByQualifier);

  private final Map<PsiClass, List<SpringBaseBeanPointer>> myBeansByClass = ConcurrentFactoryMap.createMap(this::computeBeansByPsiClass);

  private final Class2BeansMap myBeansByEffectiveClassWithInheritance = new Class2BeansMap() {
    @Override
    protected void compute(PsiClass psiClass, List<SpringBaseBeanPointer> pointers) {
      Collection<? extends SpringBaseBeanPointer> beans = getAllCommonBeans();
      for (SpringBaseBeanPointer bean : beans) {
        for (PsiClass beanClass : bean.getEffectiveBeanType()) {
          if (InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
            pointers.add(bean);
          }
        }
      }
    }
  };

  private final AtomicNotNullLazyValue<MultiMap<PsiClass, SpringBaseBeanPointer>> myBeansByClassWithInheritance =
    new AtomicNotNullLazyValue<>() {
      @Nonnull
      @Override
      protected MultiMap<PsiClass, SpringBaseBeanPointer> compute() {
        return computeBeansByPsiClassWithInheritance();
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<String, XmlTag>> myCustomBeanIdCandidates = new AtomicNotNullLazyValue<>() {
    @Nonnull
    @Override
    protected MultiMap<String, XmlTag> compute() {
      MultiMap<String, XmlTag> map = new MultiMap<>();
      for (DomFileElement<Beans> element : getRoots()) {
        for (CustomBeanWrapper bean : DomUtil.getDefinedChildrenOfType(element.getRootElement(), CustomBeanWrapper.class)) {
          if (!bean.isParsed()) {
            XmlTag tag = bean.getXmlTag();
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
    new AtomicNotNullLazyValue<>() {
      @Override
      @Nonnull
      protected Collection<SpringBaseBeanPointer> compute() {
        Collection<SpringBaseBeanPointer> beans = null;
        for (DomFileElement<Beans> element : getRoots()) {
          List<CommonSpringBean> springBeanList = SpringUtils.getChildBeans(element.getRootElement(), false);
          if (beans == null) {
            beans = new ArrayList<>(springBeanList.size());
          }
          for (CommonSpringBean bean : springBeanList) {
            beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
          }
        }
        return beans == null ? Collections.<SpringBaseBeanPointer>emptySet() : beans;
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>> myDirectInheritorsMap =
    new AtomicNotNullLazyValue<>() {
      @Override
      @Nonnull
      protected MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> compute() {
        MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = MultiMap.createConcurrent();
        for (SpringBaseBeanPointer pointer : getAllDomBeans()) {
          SpringBeanPointer parentPointer = pointer.getParentPointer();
          if (parentPointer != null) {
            map.putValue(parentPointer.getBasePointer(), pointer);
          }
        }
        return map;
      }
    };

  private interface ModelVisitor {


    /**
     * @param model
     * @return false to stop traversing
     */
    boolean visit(SpringModel model);
  }

  public BaseSpringModel(Module module, SpringFileSet fileSet) {
    myFileSet = fileSet;
    myModule = module;
  }

  private boolean visitDependencies(ModelVisitor visitor) {
    for (SpringModel dependency : myDependencies) {
      if (!visitor.visit(dependency)) {
        return false;
      }
      if (dependency instanceof BaseSpringModel baseSpringModel) {
        baseSpringModel.visitDependencies(visitor);
      }
    }
    return true;
  }

  public List<Alias> getAliases(boolean withDeps) {
    ArrayList<Alias> list = new ArrayList<>();
    ModelVisitor modelVisitor = model -> {
      for (DomFileElement<Beans> fileElement : model.getRoots()) {
        list.addAll(fileElement.getRootElement().getAliases());
      }
      return true;
    };
    modelVisitor.visit(this);
    if (withDeps) {
      visitDependencies(modelVisitor);
    }
    return list;
  }

  @Override
  @Nonnull
  public String getId() {
    return myFileSet.getId();
  }

  @Override
  public SpringFileSet getFileSet() {
    return myFileSet;
  }

  @Override
  @Nonnull
  public SpringModel[] getDependencies() {
    return myDependencies == null ? EMPTY_ARRAY : myDependencies;
  }

  @Override
  @Nonnull
  public Collection<XmlTag> getCustomBeanCandidates(String id) {
    return myCustomBeanIdCandidates.getValue().get(id);
  }

  public void setDependencies(@Nonnull SpringModel[] dependencies) {
    myDependencies = dependencies;
  }

  @Override
  @Nullable
  public SpringBeanPointer findBean(@NonNls @Nonnull String beanName) {
    return getBeanNamesMapper().getBean(beanName);
  }

  @Override
  @Nullable
  public SpringBeanPointer findParentBean(@NonNls @Nonnull String beanName) {
    for (SpringModel dependency : myDependencies) {
      SpringBeanPointer springBean = dependency.findBean(beanName);
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

  @Override
  @Nonnull
  public Collection<SpringBaseBeanPointer> getAllDomBeans() {
    return getAllDomBeans(true);
  }

  @Override
  @Nonnull
  public Collection<SpringBaseBeanPointer> getOwnBeans() {
    return myOwnBeans.getValue();
  }

  @Override
  @Nonnull
  public Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDependencies) {

    Collection<SpringBaseBeanPointer> ownBeans = getOwnBeans();
    if (withDependencies) {
      List<SpringBaseBeanPointer> allBeans = new ArrayList<>(ownBeans);
      visitDependencies(model -> {
        allBeans.addAll(model.getOwnBeans());
        return true;
      });
      return allBeans;
    }
    else {
      return ownBeans;
    }
  }

  @Override
  @Nonnull
  public Set<String> getAllBeanNames(@Nonnull String beanName) {
    return getBeanNamesMapper().getAllBeanNames(beanName);
  }

  @Override
  public boolean isNameDuplicated(@Nonnull String beanName) {
    return getBeanNamesMapper().isNameDuplicated(beanName);
  }

  @Override
  @Nonnull
  public synchronized Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(boolean withDepenedencies) {
    if (!withDepenedencies || myDependencies.length == 0) {
      return myBeansWithoutDependencies == null
        ? myBeansWithoutDependencies = calculateBeans(withDepenedencies)
        : myBeansWithoutDependencies;
    }
    else {
      return myBeans == null ? myBeans = calculateBeans(withDepenedencies) : myBeans;
    }
  }

  private Collection<SpringBaseBeanPointer> calculateBeans(boolean withDepenedencies) {
    Collection<SpringBaseBeanPointer> domBeans = getAllDomBeans(withDepenedencies);
    Collection<SpringBaseBeanPointer> allBeans = new ArrayList<>(domBeans);

    processNonDomBeans(bean -> {
      ProgressIndicatorProvider.checkCanceled();

      allBeans.add(SpringBeanPointer.createSpringBeanPointer(bean));
    });

    return allBeans;
  }

  private void processNonDomBeans(Consumer<CommonSpringBean> consumer) {
    Consumer<SpringJamElement> elementConsumer = conf -> {
      List<? extends SpringJavaBean> beans = conf.getBeans();
      for (SpringJavaBean javaBean : beans) {
        if (javaBean.isPublic()) {
          consumer.accept(javaBean);
        }
      }
    };

    processBeans(elementConsumer);

    SpringJamUtils.processAllStereotypeJavaBeans(this, consumer);
  }

  protected void processBeans(Consumer<SpringJamElement> consumer) {
    SpringJamUtils.processConfigurations(this, consumer);
  }

  @Override
  @Nonnull
  public Collection<? extends SpringBaseBeanPointer> getAllParentBeans() {
    Collection<SpringBaseBeanPointer> allBeans = new ArrayList<>();

    visitDependencies(model -> {
      allBeans.addAll(model.getAllCommonBeans());
      return true;
    });

    return allBeans;
  }

  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> findQualifiedBeans(@Nonnull SpringQualifier qualifier) {
    List<SpringBaseBeanPointer> pointers = new ArrayList<>(myBeansByQualifier.get(qualifier));
    visitDependencies(model -> {
      pointers.addAll(((BaseSpringModel)model).myBeansByQualifier.get(qualifier));
      return true;
    });
    return pointers;
  }

  private List<SpringBaseBeanPointer> computeBeansByQualifier(SpringQualifier pair) {
    List<SpringBaseBeanPointer> beans = new ArrayList<>();
    Collection<? extends SpringBaseBeanPointer> pointers = getAllCommonBeans(true);
    for (SpringBaseBeanPointer beanPointer : pointers) {
      CommonSpringBean bean = beanPointer.getSpringBean();
      SpringQualifier qualifier = bean.getSpringQualifier();
      if (qualifier != null) {
        if (SpringUtils.compareQualifiers(qualifier, pair)) {
          beans.add(beanPointer);
        }
      }
    }
    return beans;
  }


  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByPsiClass(@Nonnull PsiClass psiClass) {
    List<SpringBaseBeanPointer> pointers = new ArrayList<>(myBeansByClass.get(psiClass));
    visitDependencies(model -> {
      pointers.addAll(((BaseSpringModel)model).myBeansByClass.get(psiClass));
      return true;
    });
    return pointers;
  }

  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@Nonnull PsiClass psiClass) {
    ArrayList<SpringBaseBeanPointer> pointers =
      new ArrayList<>(myBeansByClassWithInheritance.getValue().get(psiClass));
    visitDependencies(model -> {
      pointers.addAll(((BaseSpringModel)model).myBeansByClassWithInheritance.getValue().get(psiClass));
      return true;
    });

    return pointers;
  }

  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@Nonnull PsiClass psiClass) {
    return collectBeans(psiClass, springModel -> springModel.myBeansByEffectiveClassWithInheritance);
  }

  private List<SpringBaseBeanPointer> collectBeans(PsiClass psiClass, Function<BaseSpringModel, Class2BeansMap> getter) {
    ArrayList<SpringBaseBeanPointer> pointers = new ArrayList<>(getter.apply(this).get(psiClass));
    visitDependencies(model -> {
      pointers.addAll(getter.apply((BaseSpringModel)model).get(psiClass));
      return true;
    });

    return pointers;
  }

  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> getChildren(@Nonnull SpringBeanPointer parent) {
    SpringBaseBeanPointer baseParent = parent.getBasePointer();
    ArrayList<SpringBaseBeanPointer> list = new ArrayList<>();
    for (SpringBaseBeanPointer bean : getAllDomBeans()) {
      SpringBeanPointer pointer = bean.getParentPointer();
      if (pointer != null && pointer.getBasePointer().equals(baseParent)) {
        list.add(bean);
      }
    }
    return list;
  }

  private static void addDescendants(MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map,
                                     SpringBaseBeanPointer current,
                                     Set<SpringBaseBeanPointer> result) {
    Collection<SpringBaseBeanPointer> pointers = map.get(current);
    for (SpringBaseBeanPointer pointer : pointers) {
      if (result.add(pointer)) {
        addDescendants(map, pointer, result);
      }
    }
  }

  @Override
  @Nonnull
  public List<SpringBaseBeanPointer> getDescendants(@Nonnull CommonSpringBean context) {
    Set<SpringBaseBeanPointer> visited = new HashSet<>();
    SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(context);
    visited.add(pointer);
    MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = myDirectInheritorsMap.getValue();
    addDescendants(map, pointer, visited);
    return new SmartList<>(visited);
  }

  private List<SpringBaseBeanPointer> computeBeansByPsiClass(@Nonnull PsiClass psiClass) {
    List<SpringBaseBeanPointer> beans = new ArrayList<>();
    Consumer<CommonSpringBean> consumer = bean -> {
      PsiClass beanClass = bean.getBeanClass();
      if (beanClass != null && beanClass.equals(psiClass)) {
        beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
      }
    };

    processAllBeans(consumer);

    return beans;
  }

  private void processAllBeans(final Consumer<CommonSpringBean> consumer) {
    SpringModelVisitor visitor = new SpringModelVisitor() {
      @Override
      public boolean visitBean(CommonSpringBean bean) {
        consumer.accept(bean);
        return true;
      }
    };
    for (DomFileElement<Beans> element : getRoots()) {
      SpringModelVisitor.visitBeans(visitor, element.getRootElement());
    }
    processNonDomBeans(consumer);
  }

  private MultiMap<PsiClass, SpringBaseBeanPointer> computeBeansByPsiClassWithInheritance() {
    MultiMap<PsiClass, SpringBaseBeanPointer> result = new MultiMap<>();
    Consumer<CommonSpringBean> consumer = bean -> {
      PsiClass beanClass = bean.getBeanClass();
      if (beanClass == null) {
        return;
      }

      SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(bean);
      InheritanceUtil.processSupers(beanClass, true, psiClass -> {
        result.putValue(psiClass, pointer);
        return true;
      });
    };

    processAllBeans(consumer);

    return result;
  }

  @Override
  @Nullable
  public Module getModule() {
    return myModule;
  }

  public String toString() {
    return getId();
  }
}
