package consulo.spring.impl.beans;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.searchEverywhere.SearchEverywhereContributor;
import consulo.searchEverywhere.SearchEverywhereContributorFactory;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.ui.ex.action.AnActionEvent;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-22
 */
@ExtensionImpl
public class SpringBeanSearchEverywhereContributorFactory implements SearchEverywhereContributorFactory<SpringBeanInfo> {
    @Override
    public @Nullable SearchEverywhereContributor<SpringBeanInfo> createContributor(AnActionEvent initEvent) {
        Project project = initEvent.getData(Project.KEY);
        if (project == null || !ModuleExtensionHelper.getInstance(project).hasModuleExtension(SpringModuleExtension.class)) {
            return null;
        }
        return new SpringBeanSearchEverywhereContributor(project);
    }
}
