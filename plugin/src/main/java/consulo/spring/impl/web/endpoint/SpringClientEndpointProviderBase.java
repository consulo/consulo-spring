package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.EndpointType;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.project.Project;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public abstract class SpringClientEndpointProviderBase extends SpringEndpointProviderBase<SpringClientEndpoint> {
    protected SpringClientEndpointProviderBase(Project project) {
        super(project);
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.HTTP_CLIENT_TYPE;
    }

    @Override
    protected SpringRequestMapping getMapping(SpringClientEndpoint endpoint) {
        return endpoint.getMapping();
    }

    @Override
    @RequiredReadAction
    public Iterable<UrlTargetInfo> getUrlTargetInfo(PsiClass group, SpringClientEndpoint endpoint) {
        return List.of(SpringClientUrlTargetInfo.create(endpoint));
    }
}
