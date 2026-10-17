package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.FrameworkPresentation;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringFeignEndpointProvider extends SpringClientEndpointProviderBase {
    private static final FrameworkPresentation PRESENTATION =
        new FrameworkPresentation("Spring-Cloud-OpenFeign", "Spring Cloud OpenFeign", SpringImplIconGroup.spring());

    @Inject
    public SpringFeignEndpointProvider(Project project) {
        super(project);
    }

    @Override
    public FrameworkPresentation getPresentation() {
        return PRESENTATION;
    }

    @Override
    @RequiredReadAction
    protected List<PsiClass> findGroups(GlobalSearchScope scope) {
        return SpringClientSearch.findFeignClients(myProject, scope);
    }

    @Override
    @RequiredReadAction
    protected List<SpringClientEndpoint> findEndpoints(PsiClass group) {
        return SpringClientSearch.findFeignEndpoints(group);
    }
}
