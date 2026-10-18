package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.EndpointType;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public abstract class SpringServerEndpointProviderBase extends SpringEndpointProviderBase<SpringRequestMapping> {
    protected SpringServerEndpointProviderBase(Project project) {
        super(project);
    }

    protected abstract boolean isApplicableStack(SpringWebStack stack);

    protected abstract String getRouterFunctionType();

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.HTTP_SERVER_TYPE;
    }

    @Override
    @RequiredReadAction
    protected boolean isApplicable(@Nullable Module module) {
        return super.isApplicable(module) && isApplicableStack(SpringWebStackDetector.detect(module));
    }

    @Override
    @RequiredReadAction
    protected List<PsiClass> findGroups(GlobalSearchScope scope) {
        Map<String, PsiClass> result = new TreeMap<>();
        addAll(result, SpringRequestMappingSearch.findControllers(myProject, scope));
        addAll(result, SpringFunctionalRouteSearch.findRouterClasses(myProject, scope, getRouterFunctionType()));
        return List.copyOf(result.values());
    }

    @Override
    @RequiredReadAction
    protected List<SpringRequestMapping> findEndpoints(PsiClass group) {
        List<SpringRequestMapping> result = new ArrayList<>(SpringRequestMappingSearch.findMappings(group));
        result.addAll(SpringFunctionalRouteSearch.findRoutes(group, getRouterFunctionType()));
        return result;
    }

    @Override
    protected SpringRequestMapping getMapping(SpringRequestMapping endpoint) {
        return endpoint;
    }

    @Override
    @RequiredReadAction
    public Iterable<UrlTargetInfo> getUrlTargetInfo(PsiClass group, SpringRequestMapping endpoint) {
        return List.of(SpringMvcUrlTargetInfo.create(endpoint));
    }

    @RequiredReadAction
    private static void addAll(Map<String, PsiClass> result, List<PsiClass> classes) {
        for (PsiClass psiClass : classes) {
            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName != null) {
                result.putIfAbsent(qualifiedName, psiClass);
            }
        }
    }
}
