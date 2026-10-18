package consulo.spring.impl.web.endpoint;

import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.FrameworkPresentation;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringWebFluxEndpointProvider extends SpringServerEndpointProviderBase {
    private static final FrameworkPresentation PRESENTATION =
        new FrameworkPresentation("Spring-WebFlux", "Spring WebFlux", SpringImplIconGroup.spring());

    @Inject
    public SpringWebFluxEndpointProvider(Project project) {
        super(project);
    }

    @Override
    public FrameworkPresentation getPresentation() {
        return PRESENTATION;
    }

    @Override
    protected boolean isApplicableStack(SpringWebStack stack) {
        return stack == SpringWebStack.REACTIVE;
    }

    @Override
    protected String getRouterFunctionType() {
        return SpringWebConstants.WEBFLUX_ROUTER_FUNCTION;
    }
}
