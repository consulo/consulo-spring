package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.ClassInheritorsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiElementFactory;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringDataRestSearch {
    private static final Set<String> QUERY_PREFIXES = Set.of("find", "read", "get", "query", "search", "stream", "count", "exists");

    private SpringDataRestSearch() {
    }

    @RequiredReadAction
    public static boolean isAvailable(@Nullable Module module) {
        return SpringModuleClasses.has(module, SpringWebConstants.REPOSITORY_REST_RESOURCE);
    }

    @RequiredReadAction
    public static List<PsiClass> findRepositories(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        PsiClass repository = JavaPsiFacade.getInstance(project).findClass(SpringWebConstants.REPOSITORY, GlobalSearchScope.allScope(project));
        if (repository == null) {
            return List.of();
        }
        Map<String, PsiClass> result = new TreeMap<>();
        ClassInheritorsSearch.search(repository, scope, true).forEach(psiClass -> {
            if (psiClass.isInterface() && isExported(psiClass)) {
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null) {
                    result.putIfAbsent(qualifiedName, psiClass);
                }
            }
            return true;
        });
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findEndpoints(PsiClass repository) {
        PsiElement declaration = repository.getNameIdentifier();
        String collectionPath = getCollectionPath(repository);
        if (declaration == null || collectionPath == null) {
            return List.of();
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(repository);
        String basePath = module == null ? null : SpringConfigValues.readValue(module, SpringWebConstants.DATA_REST_BASE_PATH);
        String collection = SpringRequestMappingSearch.join(basePath == null ? "" : basePath, collectionPath);
        String item = SpringRequestMappingSearch.join(collection, "{id}");

        List<SpringRequestMapping> result = new ArrayList<>();
        add(result, repository, "findAll", declaration, collection, "GET");
        add(result, repository, "save", declaration, collection, "POST");
        add(result, repository, "findById", declaration, item, "GET");
        add(result, repository, "save", declaration, item, "PUT");
        add(result, repository, "save", declaration, item, "PATCH");
        add(result, repository, "deleteById", declaration, item, "DELETE");

        String search = SpringRequestMappingSearch.join(collection, "search");
        for (PsiMethod method : repository.getMethods()) {
            PsiElement nameIdentifier = method.getNameIdentifier();
            if (nameIdentifier == null || !isQueryMethod(method) || !isExported(method)) {
                continue;
            }
            String path = readPath(method.getModifierList().findAnnotation(SpringWebConstants.REST_RESOURCE));
            result.add(SpringRequestMapping.route(method, nameIdentifier, SpringRequestMappingSearch.join(search, path != null ? path : method.getName()), "GET"));
        }
        return result;
    }

    @RequiredReadAction
    private static void add(List<SpringRequestMapping> result, PsiClass repository, String methodName, PsiElement declaration, String path, String httpMethod) {
        PsiMethod[] methods = repository.findMethodsByName(methodName, true);
        if (methods.length == 0 || !isExported(methods[0])) {
            return;
        }
        result.add(SpringRequestMapping.route(methods[0], declaration, path, httpMethod));
    }

    @RequiredReadAction
    private static @Nullable String getCollectionPath(PsiClass repository) {
        String path = readPath(repository.getModifierList() == null ? null : repository.getModifierList().findAnnotation(SpringWebConstants.REPOSITORY_REST_RESOURCE));
        if (path != null) {
            return path;
        }
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(repository.getProject());
        PsiClassType repositoryType = factory.createType(repository);
        PsiType domainType = PsiUtil.substituteTypeParameter(repositoryType, SpringWebConstants.REPOSITORY, 0, false);
        if (!(domainType instanceof PsiClassType classType)) {
            return null;
        }
        String name = classType.getClassName();
        return name == null || name.isEmpty() ? null : SpringPluralUtil.uncapitalizedPlural(name);
    }

    @RequiredReadAction
    private static @Nullable String readPath(@Nullable PsiAnnotation annotation) {
        if (annotation == null) {
            return null;
        }
        List<String> paths = SpringRequestMappingSearch.readStrings(annotation, "path");
        return paths.isEmpty() ? null : paths.get(0);
    }

    @RequiredReadAction
    private static boolean isExported(PsiClass repository) {
        if (repository.getModifierList() == null
            || repository.getModifierList().findAnnotation(SpringWebConstants.NO_REPOSITORY_BEAN) != null
            || !repository.hasModifierProperty(PsiModifier.PUBLIC)) {
            return false;
        }
        return isExportedFlag(repository.getModifierList().findAnnotation(SpringWebConstants.REPOSITORY_REST_RESOURCE));
    }

    @RequiredReadAction
    private static boolean isExported(PsiMethod method) {
        return isExportedFlag(method.getModifierList().findAnnotation(SpringWebConstants.REST_RESOURCE));
    }

    @RequiredReadAction
    private static boolean isExportedFlag(@Nullable PsiAnnotation annotation) {
        if (annotation == null) {
            return true;
        }
        PsiAnnotationMemberValue exported = annotation.findDeclaredAttributeValue("exported");
        return !(exported instanceof PsiLiteralExpression literal) || !Boolean.FALSE.equals(literal.getValue());
    }

    private static boolean isQueryMethod(PsiMethod method) {
        String name = method.getName();
        for (String prefix : QUERY_PREFIXES) {
            if (name.startsWith(prefix) && name.length() > prefix.length() && Character.isUpperCase(name.charAt(prefix.length()))) {
                return true;
            }
        }
        return false;
    }
}
