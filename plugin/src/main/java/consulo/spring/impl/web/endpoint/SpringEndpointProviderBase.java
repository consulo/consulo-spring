package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.CodeInsightColors;
import consulo.component.util.ModificationTracker;
import consulo.endpoint.EndpointFilter;
import consulo.endpoint.EndpointUrlTargetProvider;
import consulo.endpoint.ModuleEndpointFilter;
import consulo.endpoint.SearchScopeEndpointFilter;
import consulo.endpoint.oas.OpenApiSpecification;
import consulo.endpoint.presentation.HttpMethodPresentation;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public abstract class SpringEndpointProviderBase<E> implements EndpointUrlTargetProvider<PsiClass, E> {
    protected final Project myProject;

    protected SpringEndpointProviderBase(Project project) {
        myProject = project;
    }

    @RequiredReadAction
    protected abstract List<PsiClass> findGroups(GlobalSearchScope scope);

    @RequiredReadAction
    protected abstract List<E> findEndpoints(PsiClass group);

    protected abstract SpringRequestMapping getMapping(E endpoint);

    @Override
    public Status getStatus() {
        return ModuleExtensionHelper.getInstance(myProject).hasModuleExtension(SpringModuleExtension.class)
            ? Status.AVAILABLE
            : Status.UNAVAILABLE;
    }

    @Override
    @RequiredReadAction
    public Iterable<PsiClass> getEndpointGroups(EndpointFilter filter) {
        if (filter instanceof ModuleEndpointFilter moduleFilter) {
            if (!hasSpring(moduleFilter.getModule())) {
                return List.of();
            }
            GlobalSearchScope scope = moduleFilter.isFromLibraries()
                ? moduleFilter.getTransitiveSearchScope()
                : moduleFilter.getContentSearchScope();
            return moduleFilter.filterByScope(findGroups(scope), PsiElement::getContainingFile);
        }
        if (filter instanceof SearchScopeEndpointFilter scopeFilter) {
            List<PsiClass> result = new ArrayList<>();
            for (PsiClass group : findGroups(scopeFilter.getContentSearchScope())) {
                if (hasSpring(ModuleUtilCore.findModuleForPsiElement(group))) {
                    result.add(group);
                }
            }
            return result;
        }
        return List.of();
    }

    @Override
    @RequiredReadAction
    public Iterable<E> getEndpoints(PsiClass group) {
        return findEndpoints(group);
    }

    @Override
    @RequiredReadAction
    public boolean isValidEndpoint(PsiClass group, E endpoint) {
        return group.isValid() && getMapping(endpoint).isValid();
    }

    @Override
    @RequiredReadAction
    public ItemPresentation getEndpointPresentation(PsiClass group, E endpoint) {
        SpringRequestMapping mapping = getMapping(endpoint);
        return new HttpMethodPresentation(
            mapping.getPath(),
            mapping.getHttpMethod(),
            group.getName(),
            getPresentation().getIcon(),
            mapping.isDeprecated() ? CodeInsightColors.DEPRECATED_ATTRIBUTES : null
        );
    }

    @Override
    @RequiredReadAction
    public @Nullable OpenApiSpecification getOpenApiSpecification(PsiClass group, E endpoint) {
        for (UrlTargetInfo target : getUrlTargetInfo(group, endpoint)) {
            if (target instanceof SpringUrlTargetInfoBase springTarget) {
                return SpringOpenApiBuilder.build(springTarget);
            }
        }
        return null;
    }

    @Override
    public ModificationTracker getModificationTracker() {
        return SpringWebModificationTracker.getInstance(myProject);
    }

    @Override
    public @Nullable PsiElement getDocumentationElement(PsiClass group, E endpoint) {
        return getMapping(endpoint).getMethod();
    }

    public static boolean hasSpring(@Nullable Module module) {
        return module != null && ModuleUtilCore.getExtension(module, SpringModuleExtension.class) != null;
    }
}
