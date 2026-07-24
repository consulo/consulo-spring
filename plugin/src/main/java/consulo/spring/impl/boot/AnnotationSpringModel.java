package consulo.spring.impl.boot;

import com.intellij.jam.JamService;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.context.ComponentScan;
import com.intellij.spring.impl.ide.model.jam.SpringJamModel;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaSpringConfigurationElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.jam.stereotype.SpringComponentScan;
import com.intellij.spring.impl.ide.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.spring.impl.boot.domOverAnnotation.AnnotatationComponentScan;
import consulo.spring.impl.boot.jam.SpringBootApplicationElement;
import consulo.spring.impl.boot.jam.SpringBootConfigurationElement;
import consulo.spring.impl.model.BaseSpringModel;
import consulo.util.lang.StringUtil;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 15-Jan-17
 */
public class AnnotationSpringModel extends BaseSpringModel implements SpringModel {
  private final SpringJamModel mySpringJamModel;
  private consulo.module.Module myModule;

  public AnnotationSpringModel(Module module, SpringFileSet fileSet) {
    super(module, fileSet);
    myModule = module;
    mySpringJamModel = SpringJamModel.getModel(module);
  }

  @Override
  public SpringBeanPointer findBean(@Nonnull String beanName) {
    // try fast direct lookup via JAM before falling back to base mapper
    SpringJamModel model = SpringJamModel.getModel(myModule);

    // search @Configuration @Bean methods
    List<SpringJamElement> configurations = model.getConfigurations();
    for (SpringJamElement configuration : configurations) {
      List<? extends SpringJavaBean> beans = configuration.getBeans();
      for (SpringJavaBean bean : beans) {
        if (beanName.equals(bean.getBeanName())) {
          return SpringBeanPointer.createSpringBeanPointer(bean);
        }
      }
    }

    // search @Component/@Service/@Repository/@Controller stereotype components
    List<? extends SpringStereotypeElement> allStereotypeComponents = model.getAllStereotypeComponents();
    for (SpringStereotypeElement component : allStereotypeComponents) {
      if (beanName.equals(component.getBeanName())) {
        return SpringBeanPointer.createSpringBeanPointer(component);
      }
    }

    // fall back to base implementation (BeanNamesMapper with aliases)
    return super.findBean(beanName);
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

  @Override
  protected void processBeans(Consumer<SpringJamElement> consumer) {
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    JamService service = JamService.getJamService(myModule.getProject());

    for (SpringBootConfigurationElement jamClassElement : service.getJamClassElements(SpringBootConfigurationElement.META,
                                                                                      SpringAnnotationsConstants.SPRING_BOOT_CONFIGURATION_ANNOTATION,
                                                                                      scope)) {
      PsiClass psiClass = jamClassElement.getPsiClass();
      // process only own spring boot class
      if (psiClass != null && getFileSet().getId().equals(psiClass.getQualifiedName())) {
        consumer.accept(jamClassElement);
      }
    }

    super.processBeans(consumer);
  }

  @Override
  public boolean isImplicitConfiguration(@Nonnull PsiClass psiClass) {
    List<SpringJamElement> configurations = mySpringJamModel.getConfigurations();
    for (SpringJamElement configuration : configurations) {
      if (configuration instanceof JavaSpringConfigurationElement javaSpringConfigurationElement) {
        if (javaSpringConfigurationElement.getImportedClasses().contains(psiClass)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<? extends ComponentScan> getComponentScans() {
    Module module = getModule();

    JamService service = JamService.getJamService(module.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);

    List<SpringBootApplicationElement> configurations =
      service.getJamClassElements(SpringBootApplicationElement.META, SpringAnnotationsConstants.SPRING_BOOT_APPLICATION_ANNOTATION, scope);

    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(module.getProject());

    List<ComponentScan> componentScans = new ArrayList<>();
    for (SpringBootApplicationElement configuration : configurations) {
      PsiClass psiClass = configuration.getPsiClass();

      String qualifiedName = psiClass.getQualifiedName();
      if (qualifiedName == null) {
        continue;
      }

      String packageName = StringUtil.getPackageName(qualifiedName);

      PsiJavaPackage aPackage = javaPsiFacade.findPackage(StringUtil.notNullize(packageName));

      if (aPackage != null) {
        componentScans.add(new AnnotatationComponentScan(List.of(aPackage)));
      }
    }

    List<? extends SpringComponentScan> annotationComponentScans = mySpringJamModel.getComponentScans();
    for (SpringComponentScan annotationComponentScan : annotationComponentScans) {
      Set<String> basePackages = annotationComponentScan.getBasePackages();

      for (String basePackage : basePackages) {
        PsiJavaPackage aPackage = javaPsiFacade.findPackage(StringUtil.notNullize(basePackage));
        if (aPackage != null) {
          componentScans.add(new AnnotatationComponentScan(List.of(aPackage)));
        }
      }
    }
    return componentScans;
  }
}
