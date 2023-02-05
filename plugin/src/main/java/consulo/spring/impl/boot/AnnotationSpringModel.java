package consulo.spring.impl.boot;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.jam.SpringJamModel;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.impl.ide.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.module.Module;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.util.xml.DomFileElement;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 15-Jan-17
 */
public class AnnotationSpringModel implements SpringModel {
  private consulo.module.Module myModule;

  public AnnotationSpringModel(Module module) {
    myModule = module;
  }

  @Nonnull
  @Override
  public String getId() {
    return null;
  }

  @Nonnull
  @Override
  public SpringModel[] getDependencies() {
    return new SpringModel[0];
  }

  @Override
  public SpringFileSet getFileSet() {
    return null;
  }

  @Nullable
  @Override
  public SpringBeanPointer findBean(@NonNls @Nonnull String beanName) {
    SpringJamModel model = SpringJamModel.getModel(myModule);

    List<SpringJavaConfiguration> configurations = model.getConfigurations();
    for (SpringJavaConfiguration configuration : configurations) {
      List<? extends SpringJavaBean> beans = configuration.getBeans();

      for (SpringJavaBean bean : beans) {
        String beanName1 = bean.getBeanName();
        if (beanName.equals(beanName1)) {
          return SpringBeanPointer.createSpringBeanPointer(bean);
        }
      }
    }

    List<? extends SpringStereotypeElement> allStereotypeComponents = model.getAllStereotypeComponents();
    for (SpringStereotypeElement allStereotypeComponent : allStereotypeComponents) {
      String beanName1 = allStereotypeComponent.getBeanName();
      if (beanName.equals(beanName1)) {
        return SpringBeanPointer.createSpringBeanPointer(allStereotypeComponent);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public SpringBeanPointer findParentBean(@NonNls @Nonnull String beanName) {
    return null;
  }

  @Nonnull
  @Override
  public Collection<SpringBaseBeanPointer> getAllDomBeans() {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDepenedencies) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Set<String> getAllBeanNames(@Nonnull String beanName) {
    return Collections.emptySet();
  }

  @Override
  public boolean isNameDuplicated(@Nonnull String beanName) {
    return false;
  }

  @Nonnull
  @Override
  public Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(boolean withDepenedencies) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<? extends SpringBaseBeanPointer> getAllCommonBeans() {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<? extends SpringBaseBeanPointer> getAllParentBeans() {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> findBeansByPsiClass(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> getChildren(@Nonnull SpringBeanPointer parent) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> getDescendants(@Nonnull CommonSpringBean context) {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public consulo.module.Module getModule() {
    return null;
  }

  @Nonnull
  @Override
  public Collection<SpringBaseBeanPointer> getOwnBeans() {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public List<SpringBaseBeanPointer> findQualifiedBeans(@Nonnull SpringQualifier qualifier) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<XmlTag> getCustomBeanCandidates(String id) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Set<XmlFile> getConfigFiles() {
    return Collections.emptySet();
  }

  @Nonnull
  @Override
  public List<DomFileElement<Beans>> getRoots() {
    return Collections.emptyList();
  }
}
