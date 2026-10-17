package consulo.spring.impl.web.endpoint;

import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.url.UrlResolver;
import consulo.endpoint.url.UrlResolverFactory;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringMvcUrlResolverFactory implements UrlResolverFactory {
    private final Project myProject;

    @Inject
    public SpringMvcUrlResolverFactory(Project project) {
        myProject = project;
    }

    @Override
    public @Nullable UrlResolver forProject() {
        if (!ModuleExtensionHelper.getInstance(myProject).hasModuleExtension(SpringModuleExtension.class)) {
            return null;
        }
        return new SpringMvcUrlResolver(myProject);
    }
}
