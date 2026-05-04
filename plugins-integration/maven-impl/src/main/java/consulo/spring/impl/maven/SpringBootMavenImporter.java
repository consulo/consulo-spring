package consulo.spring.impl.maven;

import consulo.annotation.component.ExtensionImpl;
import consulo.maven.importing.MavenImporterFromDependency;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2024-04-14
 */
@ExtensionImpl
public class SpringBootMavenImporter extends MavenImporterFromDependency {
  public SpringBootMavenImporter() {
    super("org.springframework.boot", "spring-boot");
  }

  @Override
  public void preProcess(Module module,
                         MavenProject mavenProject,
                         MavenProjectChanges mavenProjectChanges,
                         MavenModifiableModelsProvider mavenModifiableModelsProvider) {

  }

  @Override
  public void process(MavenModifiableModelsProvider mavenModifiableModelsProvider,
                      Module module,
                      MavenRootModelAdapter mavenRootModelAdapter,
                      MavenProjectsTree mavenProjectsTree,
                      MavenProject mavenProject,
                      MavenProjectChanges mavenProjectChanges,
                      Map<MavenProject, String> map,
                      List<MavenProjectsProcessorTask> list) {
    ModifiableRootModel rootModel = mavenModifiableModelsProvider.getRootModel(module);
    MutableModuleExtension extensionWithoutCheck = rootModel.getExtensionWithoutCheck("spring");
    extensionWithoutCheck.setEnabled(true);
  }
}
