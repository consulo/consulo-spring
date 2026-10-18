package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.project.content.scope.ProjectScopes;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringActuatorSearch {
    private static final List<String> ENDPOINT_ANNOTATIONS = List.of(SpringWebConstants.ACTUATOR_ENDPOINT, SpringWebConstants.ACTUATOR_WEB_ENDPOINT);
    private static final String ALL = "*";

    private SpringActuatorSearch() {
    }

    @RequiredReadAction
    public static boolean isAvailable(@Nullable Module module) {
        return SpringModuleClasses.has(module, SpringWebConstants.ACTUATOR_ENDPOINT);
    }

    @RequiredReadAction
    public static List<PsiClass> findEndpointClasses(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        Map<String, PsiClass> result = new TreeMap<>();
        for (String annotationName : ENDPOINT_ANNOTATIONS) {
            PsiClass annotationClass = facade.findClass(annotationName, allScope);
            if (annotationClass == null) {
                continue;
            }
            AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).forEach(psiClass -> {
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null && !psiClass.isAnnotationType()) {
                    result.putIfAbsent(qualifiedName, psiClass);
                }
                return true;
            });
        }
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findOperations(PsiClass endpointClass) {
        String id = getEndpointId(endpointClass);
        if (id == null) {
            return List.of();
        }
        Module ownModule = ModuleUtilCore.findModuleForPsiElement(endpointClass);
        Module module = ownModule != null ? ownModule : findApplicationModule(endpointClass.getProject());
        if (isLibrary(endpointClass) && !isExposed(module, id)) {
            return List.of();
        }
        String basePath = module == null ? null : SpringConfigValues.readValue(module, SpringWebConstants.MANAGEMENT_BASE_PATH);
        String endpointPath = SpringRequestMappingSearch.join(basePath == null ? SpringWebConstants.DEFAULT_MANAGEMENT_BASE_PATH : basePath, id);

        List<SpringRequestMapping> result = new ArrayList<>();
        for (PsiMethod method : endpointClass.getMethods()) {
            PsiElement nameIdentifier = method.getNameIdentifier();
            String httpMethod = getHttpMethod(method);
            if (nameIdentifier == null || httpMethod == null) {
                continue;
            }
            StringBuilder path = new StringBuilder(endpointPath);
            for (PsiParameter parameter : method.getParameterList().getParameters()) {
                if (SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.ACTUATOR_SELECTOR) != null) {
                    path.append("/{").append(parameter.getName()).append('}');
                }
            }
            result.add(SpringRequestMapping.route(method, nameIdentifier, path.toString(), httpMethod));
        }
        return result;
    }

    @RequiredReadAction
    private static @Nullable Module findApplicationModule(Project project) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (SpringEndpointProviderBase.hasSpring(module) && isAvailable(module)) {
                return module;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable String getEndpointId(PsiClass endpointClass) {
        PsiModifierList modifierList = endpointClass.getModifierList();
        if (modifierList == null) {
            return null;
        }
        for (String annotationName : ENDPOINT_ANNOTATIONS) {
            PsiAnnotation annotation = modifierList.findAnnotation(annotationName);
            if (annotation != null) {
                List<String> ids = SpringRequestMappingSearch.readStrings(annotation, "id");
                return ids.isEmpty() ? null : ids.get(0);
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable String getHttpMethod(PsiMethod method) {
        for (PsiAnnotation annotation : method.getModifierList().getAnnotations()) {
            String httpMethod = SpringWebConstants.ACTUATOR_OPERATIONS.get(annotation.getQualifiedName());
            if (httpMethod != null) {
                return httpMethod;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isExposed(@Nullable Module module, String id) {
        String include = module == null ? null : SpringConfigValues.readValue(module, SpringWebConstants.MANAGEMENT_EXPOSURE_INCLUDE);
        Set<String> exposed = new LinkedHashSet<>();
        for (String value : (include == null ? SpringWebConstants.DEFAULT_MANAGEMENT_EXPOSURE : include).split(",")) {
            exposed.add(value.trim());
        }
        return exposed.contains(ALL) || exposed.contains(id);
    }

    @RequiredReadAction
    private static boolean isLibrary(PsiClass psiClass) {
        VirtualFile file = psiClass.getContainingFile() == null ? null : psiClass.getContainingFile().getVirtualFile();
        return file != null && ProjectScopes.getLibrariesScope(psiClass.getProject()).contains(file);
    }
}
