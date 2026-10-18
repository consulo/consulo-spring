package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.endpoint.url.Authority;
import consulo.endpoint.url.HttpUrlResolver;
import consulo.endpoint.url.UrlResolveRequest;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.DumbService;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringMvcUrlResolver extends HttpUrlResolver {
    private static final List<String> ROUTER_FUNCTION_TYPES =
        List.of(SpringWebConstants.MVC_ROUTER_FUNCTION, SpringWebConstants.WEBFLUX_ROUTER_FUNCTION);

    private final Project myProject;

    public SpringMvcUrlResolver(Project project) {
        myProject = project;
    }

    @Override
    @RequiredReadAction
    public Iterable<UrlTargetInfo> resolve(UrlResolveRequest request) {
        String authority = request.getAuthorityHint();
        String method = request.getMethod();
        List<UrlTargetInfo> result = new ArrayList<>();
        for (UrlTargetInfo target : getVariants()) {
            if (!matchesAuthority(target, authority)) {
                continue;
            }
            if (method != null && !target.getMethods().isEmpty() && !target.getMethods().contains(method)) {
                continue;
            }
            if (target.getPath().isCompatibleWith(request.getPath())
                || target instanceof SpringMvcUrlTargetInfo springTarget && springTarget.getMappingPath().isCompatibleWith(request.getPath())) {
                result.add(target);
            }
        }
        return result;
    }

    @Override
    @RequiredReadAction
    public Iterable<UrlTargetInfo> getVariants() {
        if (DumbService.isDumb(myProject)) {
            return List.of();
        }
        return CachedValuesManager.getManager(myProject).getCachedValue(myProject, () -> CachedValueProvider.Result.create(
            collectTargets(myProject),
            SpringWebModificationTracker.getInstance(myProject)
        ));
    }

    @RequiredReadAction
    private static List<UrlTargetInfo> collectTargets(Project project) {
        List<UrlTargetInfo> result = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (!SpringEndpointProviderBase.hasSpring(module)) {
                continue;
            }
            GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
            for (PsiClass controller : SpringRequestMappingSearch.findControllers(project, scope)) {
                for (SpringRequestMapping mapping : SpringRequestMappingSearch.findMappings(controller)) {
                    result.add(SpringMvcUrlTargetInfo.create(mapping));
                }
            }
            for (String routerFunctionType : ROUTER_FUNCTION_TYPES) {
                for (PsiClass routerClass : SpringFunctionalRouteSearch.findRouterClasses(project, scope, routerFunctionType)) {
                    for (SpringRequestMapping mapping : SpringFunctionalRouteSearch.findRoutes(routerClass, routerFunctionType)) {
                        result.add(SpringMvcUrlTargetInfo.create(mapping));
                    }
                }
            }
        }
        return List.copyOf(result);
    }

    private static boolean matchesAuthority(UrlTargetInfo target, @Nullable String authority) {
        if (authority == null) {
            return true;
        }
        for (Authority.Exact known : HTTP_AUTHORITY) {
            if (known.getText().equals(authority)) {
                return true;
            }
        }
        for (Authority targetAuthority : target.getAuthorities()) {
            if (targetAuthority instanceof Authority.Exact exact && exact.getText().equals(authority)) {
                return true;
            }
        }
        return false;
    }
}
