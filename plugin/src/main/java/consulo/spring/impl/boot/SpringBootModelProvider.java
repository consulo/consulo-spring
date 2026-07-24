package consulo.spring.impl.boot;

import com.intellij.jam.JamService;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.SpringModelProvider;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.spring.impl.boot.jam.SpringBootApplicationElement;
import consulo.spring.impl.boot.jam.SpringBootConfigurationElement;
import consulo.spring.impl.module.extension.SpringModuleExtension;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 15-Jan-17
 */
@ExtensionImpl
public class SpringBootModelProvider implements SpringModelProvider {
  @RequiredReadAction
  @Override
  public void collectFilesets(@Nonnull SpringModuleExtension extension, @Nonnull Consumer<SpringFileSet> consumer) {
    Module module = extension.getModule();

    JamService service = JamService.getJamService(module.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);

    List<SpringJamElement> elements = new ArrayList<>();
    elements.addAll(service.getJamClassElements(SpringBootApplicationElement.META,
                                                SpringAnnotationsConstants.SPRING_BOOT_APPLICATION_ANNOTATION,
                                                scope));
    elements.addAll(service.getJamClassElements(SpringBootConfigurationElement.META,
                                                SpringAnnotationsConstants.SPRING_BOOT_CONFIGURATION_ANNOTATION,
                                                scope));

    if (elements.isEmpty()) {
      return;
    }

    for (SpringJamElement configuration : elements) {
      PsiClass psiClass = configuration.getPsiClass();

      String qualifiedName = psiClass.getQualifiedName();
      if (qualifiedName == null) {
        continue;
      }

      consumer.accept(new SpringBootFileSet(qualifiedName, psiClass.getName(), extension));
    }
  }

  @Nonnull
  @Override
  public SpringModel createModel(@Nonnull Module module, @Nonnull SpringFileSet fileSet) {
    return new AnnotationSpringModel(module, fileSet);
  }
}
