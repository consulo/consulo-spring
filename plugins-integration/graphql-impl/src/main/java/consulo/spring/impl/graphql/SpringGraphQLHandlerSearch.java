package consulo.spring.impl.graphql;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringGraphQLHandlerSearch {
    private static final String[] NAME_ATTRIBUTES = {"name", "value"};
    private static final String[] FIELD_ATTRIBUTES = {"field", "value"};

    private SpringGraphQLHandlerSearch() {
    }

    @RequiredReadAction
    public static boolean isAvailable(@Nullable Module module) {
        return module != null && JavaPsiFacade.getInstance(module.getProject())
            .findClass(SpringGraphQLConstants.QUERY_MAPPING, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)) != null;
    }

    @RequiredReadAction
    public static List<PsiClass> findHandlerClasses(Project project, GlobalSearchScope scope) {
        Map<String, PsiClass> result = new TreeMap<>();
        for (PsiMethod method : findHandlerMethods(project, scope)) {
            PsiClass containingClass = method.getContainingClass();
            String qualifiedName = containingClass == null ? null : containingClass.getQualifiedName();
            if (qualifiedName != null) {
                result.putIfAbsent(qualifiedName, containingClass);
            }
        }
        return new ArrayList<>(result.values());
    }

    @RequiredReadAction
    public static List<SpringGraphQLHandler> findHandlers(PsiClass psiClass) {
        List<SpringGraphQLHandler> result = new ArrayList<>();
        for (PsiMethod method : psiClass.getMethods()) {
            SpringGraphQLHandler handler = getHandler(method);
            if (handler != null) {
                result.add(handler);
            }
        }
        return result;
    }

    @RequiredReadAction
    public static Collection<SpringGraphQLHandler> findHandlers(Project project, String typeName, String fieldName) {
        Map<String, List<SpringGraphQLHandler>> handlers = CachedValuesManager.getManager(project).getCachedValue(
            project,
            () -> CachedValueProvider.Result.create(collectProjectHandlers(project), PsiModificationTracker.getInstance(project))
        );
        return handlers.getOrDefault(typeName + "." + fieldName, List.of());
    }

    @RequiredReadAction
    public static @Nullable SpringGraphQLHandler getHandler(PsiMethod method) {
        for (String annotationName : SpringGraphQLConstants.HANDLER_ANNOTATIONS) {
            PsiAnnotation annotation = method.getModifierList().findAnnotation(annotationName);
            if (annotation != null) {
                return createHandler(method, annotation, annotationName);
            }
        }
        return null;
    }

    @RequiredReadAction
    private static Map<String, List<SpringGraphQLHandler>> collectProjectHandlers(Project project) {
        Map<String, List<SpringGraphQLHandler>> result = new LinkedHashMap<>();
        for (PsiMethod method : findHandlerMethods(project, GlobalSearchScope.projectScope(project))) {
            SpringGraphQLHandler handler = getHandler(method);
            if (handler != null) {
                result.computeIfAbsent(handler.getPresentableName(), key -> new ArrayList<>()).add(handler);
            }
        }
        return result;
    }

    @RequiredReadAction
    private static List<PsiMethod> findHandlerMethods(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }

        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        Set<PsiMethod> methods = new LinkedHashSet<>();
        for (String annotationName : SpringGraphQLConstants.HANDLER_ANNOTATIONS) {
            PsiClass annotationClass = facade.findClass(annotationName, allScope);
            if (annotationClass != null) {
                AnnotatedElementsSearch.searchPsiMethods(annotationClass, scope).forEach(method -> {
                    methods.add(method);
                    return true;
                });
            }
        }
        return new ArrayList<>(methods);
    }

    @RequiredReadAction
    private static @Nullable SpringGraphQLHandler createHandler(PsiMethod method, PsiAnnotation annotation, String annotationName) {
        String typeName = switch (annotationName) {
            case SpringGraphQLConstants.QUERY_MAPPING -> SpringGraphQLConstants.QUERY_TYPE;
            case SpringGraphQLConstants.MUTATION_MAPPING -> SpringGraphQLConstants.MUTATION_TYPE;
            case SpringGraphQLConstants.SUBSCRIPTION_MAPPING -> SpringGraphQLConstants.SUBSCRIPTION_TYPE;
            default -> resolveTypeName(method, annotation, SpringGraphQLConstants.BATCH_MAPPING.equals(annotationName));
        };
        if (typeName == null) {
            return null;
        }

        boolean schemaMapping = SpringGraphQLConstants.SCHEMA_MAPPING.equals(annotationName)
            || SpringGraphQLConstants.BATCH_MAPPING.equals(annotationName);
        String fieldName = readAttribute(annotation, schemaMapping ? FIELD_ATTRIBUTES : NAME_ATTRIBUTES);
        return new SpringGraphQLHandler(method, typeName, fieldName != null ? fieldName : method.getName());
    }

    @RequiredReadAction
    private static @Nullable String resolveTypeName(PsiMethod method, PsiAnnotation annotation, boolean batch) {
        String typeName = AnnotationUtil.getDeclaredStringAttributeValue(annotation, "typeName");
        if (!StringUtil.isEmpty(typeName)) {
            return typeName;
        }

        PsiClass containingClass = method.getContainingClass();
        PsiAnnotation classMapping = AnnotationUtil.findAnnotation(containingClass, SpringGraphQLConstants.SCHEMA_MAPPING);
        if (classMapping != null) {
            String classTypeName = AnnotationUtil.getDeclaredStringAttributeValue(classMapping, "typeName");
            if (!StringUtil.isEmpty(classTypeName)) {
                return classTypeName;
            }
        }

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            if (AnnotationUtil.findAnnotation(parameter, SpringGraphQLConstants.NON_SOURCE_PARAMETER_ANNOTATIONS.toArray(String[]::new)) != null) {
                continue;
            }
            PsiType type = batch ? PsiUtil.extractIterableTypeParameter(parameter.getType(), false) : parameter.getType();
            PsiClass sourceClass = type instanceof PsiClassType classType ? classType.resolve() : null;
            String qualifiedName = sourceClass == null ? null : sourceClass.getQualifiedName();
            if (qualifiedName == null || isFrameworkType(qualifiedName)) {
                continue;
            }
            return sourceClass.getName();
        }
        return null;
    }

    private static boolean isFrameworkType(String qualifiedName) {
        return qualifiedName.startsWith("java.")
            || qualifiedName.startsWith("graphql.")
            || qualifiedName.startsWith("org.springframework.")
            || qualifiedName.startsWith("reactor.");
    }

    private static @Nullable String readAttribute(PsiAnnotation annotation, String[] attributes) {
        for (String attribute : attributes) {
            String value = AnnotationUtil.getDeclaredStringAttributeValue(annotation, attribute);
            if (!StringUtil.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }
}
