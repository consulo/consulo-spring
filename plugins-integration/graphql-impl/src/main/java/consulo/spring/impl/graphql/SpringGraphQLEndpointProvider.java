package consulo.spring.impl.graphql;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.component.util.ModificationTracker;
import consulo.endpoint.EndpointFilter;
import consulo.endpoint.EndpointProvider;
import consulo.endpoint.EndpointType;
import consulo.endpoint.FrameworkPresentation;
import consulo.endpoint.ModuleEndpointFilter;
import consulo.endpoint.SearchScopeEndpointFilter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.navigation.ItemPresentation;
import consulo.project.Project;
import consulo.ui.ex.tree.PresentationData;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringGraphQLEndpointProvider implements EndpointProvider<PsiClass, SpringGraphQLHandler> {
    private static final FrameworkPresentation PRESENTATION =
        new FrameworkPresentation("Spring-GraphQL", "Spring for GraphQL", GraphQLIcons.FILE);

    private final Project myProject;

    @Inject
    public SpringGraphQLEndpointProvider(Project project) {
        myProject = project;
    }

    @Override
    public EndpointType getEndpointType() {
        return EndpointType.GRAPH_QL_TYPE;
    }

    @Override
    public FrameworkPresentation getPresentation() {
        return PRESENTATION;
    }

    @Override
    public Status getStatus() {
        for (Module module : ModuleManager.getInstance(myProject).getModules()) {
            if (module.getExtension(SpringGraphQLConstants.SPRING_MODULE_EXTENSION_ID) != null) {
                return Status.AVAILABLE;
            }
        }
        return Status.UNAVAILABLE;
    }

    @Override
    @RequiredReadAction
    public Iterable<PsiClass> getEndpointGroups(EndpointFilter filter) {
        if (filter instanceof ModuleEndpointFilter moduleFilter) {
            if (!SpringGraphQLHandlerSearch.isAvailable(moduleFilter.getModule())) {
                return List.of();
            }
            GlobalSearchScope scope = moduleFilter.isFromLibraries()
                ? moduleFilter.getTransitiveSearchScope()
                : moduleFilter.getContentSearchScope();
            return moduleFilter.filterByScope(SpringGraphQLHandlerSearch.findHandlerClasses(myProject, scope), PsiElement::getContainingFile);
        }
        if (filter instanceof SearchScopeEndpointFilter scopeFilter) {
            List<PsiClass> result = new ArrayList<>();
            for (PsiClass group : SpringGraphQLHandlerSearch.findHandlerClasses(myProject, scopeFilter.getContentSearchScope())) {
                if (SpringGraphQLHandlerSearch.isAvailable(ModuleUtilCore.findModuleForPsiElement(group))) {
                    result.add(group);
                }
            }
            return result;
        }
        return List.of();
    }

    @Override
    @RequiredReadAction
    public Iterable<SpringGraphQLHandler> getEndpoints(PsiClass group) {
        return SpringGraphQLHandlerSearch.findHandlers(group);
    }

    @Override
    @RequiredReadAction
    public boolean isValidEndpoint(PsiClass group, SpringGraphQLHandler endpoint) {
        return group.isValid() && endpoint.isValid();
    }

    @Override
    @RequiredReadAction
    public ItemPresentation getEndpointPresentation(PsiClass group, SpringGraphQLHandler endpoint) {
        return new PresentationData(endpoint.getPresentableName(), group.getName(), GraphQLIcons.FILE, null);
    }

    @Override
    public ModificationTracker getModificationTracker() {
        return PsiModificationTracker.getInstance(myProject);
    }

    @Override
    @RequiredReadAction
    public @Nullable PsiElement getDocumentationElement(PsiClass group, SpringGraphQLHandler endpoint) {
        for (GraphQLFieldDefinition field : SpringGraphQLSchemaSearch.findFieldDefinitions(endpoint)) {
            PsiElement nameIdentifier = field.getNameIdentifier();
            if (nameIdentifier != null) {
                return nameIdentifier;
            }
        }
        return endpoint.getMethod();
    }

    @Override
    public @Nullable PsiElement getNavigationElement(PsiClass group, SpringGraphQLHandler endpoint) {
        return endpoint.getMethod();
    }
}
