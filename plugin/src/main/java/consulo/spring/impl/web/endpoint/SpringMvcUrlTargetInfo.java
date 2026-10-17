package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.Authority;
import consulo.endpoint.url.HttpUrlResolver;
import consulo.endpoint.url.UrlConstants;
import consulo.endpoint.url.UrlPath;
import consulo.endpoint.url.reference.DefaultFrameworkUrlPathSpecification;
import consulo.language.util.ModuleUtilCore;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.ui.image.Image;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringMvcUrlTargetInfo extends SpringUrlTargetInfoBase {
    private final UrlPath myMappingPath;

    @RequiredReadAction
    private SpringMvcUrlTargetInfo(
        List<Authority> authorities,
        String pathText,
        UrlPath mappingPath,
        SpringRequestMapping mapping,
        List<SpringQueryParameter> queryParameters,
        Set<String> contentTypes
    ) {
        super(UrlConstants.HTTP_SCHEMES, authorities, pathText, mapping, queryParameters, contentTypes);
        myMappingPath = mappingPath;
    }

    @RequiredReadAction
    public static SpringMvcUrlTargetInfo create(SpringRequestMapping mapping) {
        SpringWebServerSettings settings = SpringWebServerSettings.get(ModuleUtilCore.findModuleForPsiElement(mapping.getMethod()));
        return new SpringMvcUrlTargetInfo(
            List.of(new Authority.Exact(settings.getAuthority()), new Authority.Exact(UrlConstants.LOCALHOST)),
            settings.applyTo(mapping.getPath()),
            DefaultFrameworkUrlPathSpecification.INSTANCE.parsePath(mapping.getPath()),
            mapping,
            SpringRequestMappingSearch.findQueryParameters(mapping.getMethod()),
            readContentTypes(mapping, "consumes")
        );
    }

    @RequiredReadAction
    public static SpringMvcUrlTargetInfo createDeclaration(SpringRequestMapping mapping) {
        UrlPath path = DefaultFrameworkUrlPathSpecification.INSTANCE.parsePath(mapping.getPath());
        return new SpringMvcUrlTargetInfo(List.copyOf(HttpUrlResolver.HTTP_AUTHORITY), mapping.getPath(), path, mapping, List.of(), Set.of());
    }

    public UrlPath getMappingPath() {
        return myMappingPath;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public Image getIcon() {
        return SpringImplIconGroup.gutterRequestmapping();
    }
}
