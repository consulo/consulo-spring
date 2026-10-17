package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.Authority;
import consulo.endpoint.url.UrlConstants;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringClientUrlTargetInfo extends SpringUrlTargetInfoBase {
    @RequiredReadAction
    private SpringClientUrlTargetInfo(List<String> schemes, List<Authority> authorities, SpringRequestMapping mapping) {
        super(
            schemes,
            authorities,
            mapping.getPath(),
            mapping,
            SpringRequestMappingSearch.findQueryParameters(mapping.getMethod()),
            readContentTypes(mapping, "consumes", "contentType")
        );
    }

    @RequiredReadAction
    public static SpringClientUrlTargetInfo create(SpringClientEndpoint endpoint) {
        SpringClientBaseUrl baseUrl = endpoint.getBaseUrl();
        String scheme = baseUrl.getScheme();
        String authority = baseUrl.getAuthority();
        return new SpringClientUrlTargetInfo(
            scheme == null ? UrlConstants.HTTP_SCHEMES : List.of(scheme),
            authority == null ? List.of(new Authority.Placeholder()) : List.of(new Authority.Exact(authority)),
            endpoint.getMapping()
        );
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public Image getIcon() {
        return PlatformIconGroup.javaeeWebserviceclient();
    }
}
