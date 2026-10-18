package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.EndpointType;
import consulo.endpoint.FrameworkPresentation;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringMessagingEndpointProvider extends SpringEndpointProviderBase<SpringRequestMapping> {
    private static final FrameworkPresentation PRESENTATION =
        new FrameworkPresentation("Spring-Messaging", "Spring Messaging", SpringImplIconGroup.spring());

    @Inject
    public SpringMessagingEndpointProvider(Project project) {
        super(project);
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.WEBSOCKET_SERVER_TYPE;
    }

    @Override
    public FrameworkPresentation getPresentation() {
        return PRESENTATION;
    }

    @Override
    @RequiredReadAction
    protected boolean isApplicable(@Nullable Module module) {
        return super.isApplicable(module) && SpringMessagingSearch.isAvailable(module);
    }

    @Override
    @RequiredReadAction
    protected List<PsiClass> findGroups(GlobalSearchScope scope) {
        return SpringMessagingSearch.findControllers(myProject, scope);
    }

    @Override
    @RequiredReadAction
    protected List<SpringRequestMapping> findEndpoints(PsiClass group) {
        return SpringMessagingSearch.findDestinations(group);
    }

    @Override
    protected SpringRequestMapping getMapping(SpringRequestMapping endpoint) {
        return endpoint;
    }

    @Override
    public boolean shouldShowOpenApiPanel() {
        return false;
    }

    @Override
    public Iterable<UrlTargetInfo> getUrlTargetInfo(PsiClass group, SpringRequestMapping endpoint) {
        return List.of();
    }
}
