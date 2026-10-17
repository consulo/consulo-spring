package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringClientSearch {
    private static final String[] EXCHANGE_PATH_ATTRIBUTES = {"value", "url"};
    private static final String[] FEIGN_NAME_ATTRIBUTES = {"value", "name", "serviceId"};

    private SpringClientSearch() {
    }

    @RequiredReadAction
    public static List<PsiClass> findFeignClients(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        PsiClass annotationClass = JavaPsiFacade.getInstance(project).findClass(SpringWebConstants.FEIGN_CLIENT, GlobalSearchScope.allScope(project));
        if (annotationClass == null) {
            return List.of();
        }
        Map<String, PsiClass> result = new TreeMap<>();
        AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).forEach(psiClass -> {
            addClient(psiClass, result);
            return true;
        });
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<PsiClass> findExchangeClients(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        Map<String, PsiClass> result = new TreeMap<>();

        PsiClass httpExchange = facade.findClass(SpringWebConstants.HTTP_EXCHANGE, allScope);
        if (httpExchange == null) {
            return List.of();
        }
        AnnotatedElementsSearch.searchPsiClasses(httpExchange, scope).forEach(psiClass -> {
            addClient(psiClass, result);
            return true;
        });

        List<PsiClass> methodAnnotations = new ArrayList<>();
        methodAnnotations.add(httpExchange);
        for (String qualifiedName : SpringWebConstants.COMPOSED_EXCHANGES.keySet()) {
            PsiClass annotationClass = facade.findClass(qualifiedName, allScope);
            if (annotationClass != null) {
                methodAnnotations.add(annotationClass);
            }
        }
        for (PsiClass annotationClass : methodAnnotations) {
            AnnotatedElementsSearch.searchPsiMethods(annotationClass, scope).forEach(method -> {
                PsiClass containingClass = method.getContainingClass();
                if (containingClass != null && !isFeignClient(containingClass)) {
                    addClient(containingClass, result);
                }
                return true;
            });
        }
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringClientEndpoint> findEndpoints(PsiClass client) {
        if (isFeignClient(client)) {
            return findFeignEndpoints(client);
        }
        return findExchangeEndpoints(client);
    }

    @RequiredReadAction
    public static List<SpringClientEndpoint> findFeignEndpoints(PsiClass client) {
        SpringClientBaseUrl baseUrl = getFeignBaseUrl(client);
        if (baseUrl == null) {
            return List.of();
        }
        List<SpringRequestMapping> mappings = SpringRequestMappingSearch.findMappings(
            client,
            List.of(getFeignPrefix(client, baseUrl)),
            SpringRequestMappingSearch::resolveMappingInfo
        );
        return wrap(mappings, baseUrl);
    }

    @RequiredReadAction
    public static List<SpringClientEndpoint> findExchangeEndpoints(PsiClass client) {
        PsiAnnotation typeExchange = findAnnotation(client, SpringWebConstants.HTTP_EXCHANGE);
        SpringClientBaseUrl baseUrl = getExchangeBaseUrl(client, typeExchange);
        List<String> typeMethods = typeExchange == null ? List.of() : readExchangeMethods(typeExchange);
        List<SpringRequestMapping> mappings = SpringRequestMappingSearch.findMappings(
            client,
            List.of(baseUrl.getPath()),
            annotation -> resolveExchangeInfo(annotation, typeMethods)
        );
        return wrap(mappings, baseUrl);
    }

    @RequiredReadAction
    public static List<SpringClientEndpoint> findEndpoints(PsiMethod method) {
        PsiClass client = method.getContainingClass();
        if (client == null) {
            return List.of();
        }
        if (isFeignClient(client)) {
            SpringClientBaseUrl baseUrl = getFeignBaseUrl(client);
            if (baseUrl == null) {
                return List.of();
            }
            return wrap(
                SpringRequestMappingSearch.findMappings(
                    method,
                    List.of(getFeignPrefix(client, baseUrl)),
                    SpringRequestMappingSearch::resolveMappingInfo
                ),
                baseUrl
            );
        }
        if (!hasExchangeAnnotation(method)) {
            return List.of();
        }
        PsiAnnotation typeExchange = findAnnotation(client, SpringWebConstants.HTTP_EXCHANGE);
        SpringClientBaseUrl baseUrl = getExchangeBaseUrl(client, typeExchange);
        List<String> typeMethods = typeExchange == null ? List.of() : readExchangeMethods(typeExchange);
        Function<PsiAnnotation, @Nullable SpringMappingInfo> resolver = annotation -> resolveExchangeInfo(annotation, typeMethods);
        return wrap(SpringRequestMappingSearch.findMappings(method, List.of(baseUrl.getPath()), resolver), baseUrl);
    }

    @RequiredReadAction
    public static boolean isClient(@Nullable PsiClass psiClass) {
        if (psiClass == null || !psiClass.isInterface()) {
            return false;
        }
        if (isFeignClient(psiClass) || findAnnotation(psiClass, SpringWebConstants.HTTP_EXCHANGE) != null) {
            return true;
        }
        for (PsiMethod method : psiClass.getMethods()) {
            if (hasExchangeAnnotation(method)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    public static boolean isFeignClient(@Nullable PsiClass psiClass) {
        return psiClass != null && psiClass.isInterface() && findAnnotation(psiClass, SpringWebConstants.FEIGN_CLIENT) != null;
    }

    @RequiredReadAction
    public static boolean isExchangeAnnotation(PsiAnnotation annotation) {
        String qualifiedName = annotation.getQualifiedName();
        return SpringWebConstants.HTTP_EXCHANGE.equals(qualifiedName) || SpringWebConstants.COMPOSED_EXCHANGES.containsKey(qualifiedName);
    }

    @RequiredReadAction
    public static boolean hasExchangeAnnotation(PsiMethod method) {
        for (PsiAnnotation annotation : method.getModifierList().getAnnotations()) {
            if (isExchangeAnnotation(annotation)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    public static @Nullable String getFeignName(PsiClass client) {
        PsiAnnotation feign = findAnnotation(client, SpringWebConstants.FEIGN_CLIENT);
        if (feign == null) {
            return null;
        }
        for (String attribute : FEIGN_NAME_ATTRIBUTES) {
            List<String> values = SpringRequestMappingSearch.readStrings(feign, attribute);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable SpringClientBaseUrl getFeignBaseUrl(PsiClass client) {
        PsiAnnotation feign = findAnnotation(client, SpringWebConstants.FEIGN_CLIENT);
        if (feign == null) {
            return null;
        }
        return SpringClientBaseUrl.parse(readResolved(client, feign, "url"), true);
    }

    @RequiredReadAction
    private static String getFeignPrefix(PsiClass client, SpringClientBaseUrl baseUrl) {
        PsiAnnotation feign = findAnnotation(client, SpringWebConstants.FEIGN_CLIENT);
        String path = feign == null ? null : readResolved(client, feign, "path");
        return SpringRequestMappingSearch.join(baseUrl.getPath(), path == null ? "" : path);
    }

    @RequiredReadAction
    private static SpringClientBaseUrl getExchangeBaseUrl(PsiClass client, @Nullable PsiAnnotation typeExchange) {
        if (typeExchange == null) {
            return SpringClientBaseUrl.RELATIVE;
        }
        List<String> urls = SpringRequestMappingSearch.readPaths(typeExchange, EXCHANGE_PATH_ATTRIBUTES, List.of());
        if (urls.isEmpty()) {
            return SpringClientBaseUrl.RELATIVE;
        }
        return SpringClientBaseUrl.parse(SpringConfigValues.resolvePlaceholders(ModuleUtilCore.findModuleForPsiElement(client), urls.get(0)), false);
    }

    @RequiredReadAction
    private static @Nullable SpringMappingInfo resolveExchangeInfo(PsiAnnotation annotation, List<String> typeMethods) {
        String qualifiedName = annotation.getQualifiedName();
        if (SpringWebConstants.HTTP_EXCHANGE.equals(qualifiedName)) {
            List<String> methods = readExchangeMethods(annotation);
            return new SpringMappingInfo(
                SpringRequestMappingSearch.readPaths(annotation, EXCHANGE_PATH_ATTRIBUTES, List.of("")),
                methods.isEmpty() ? typeMethods : methods
            );
        }
        String composedMethod = qualifiedName == null ? null : SpringWebConstants.COMPOSED_EXCHANGES.get(qualifiedName);
        if (composedMethod == null) {
            return null;
        }
        return new SpringMappingInfo(SpringRequestMappingSearch.readPaths(annotation, EXCHANGE_PATH_ATTRIBUTES, List.of("")), List.of(composedMethod));
    }

    @RequiredReadAction
    private static List<String> readExchangeMethods(PsiAnnotation annotation) {
        List<String> result = new ArrayList<>();
        for (String method : SpringRequestMappingSearch.readStrings(annotation, "method")) {
            result.add(method.trim().toUpperCase(Locale.ROOT));
        }
        return result;
    }

    @RequiredReadAction
    private static @Nullable String readResolved(PsiClass client, PsiAnnotation annotation, String attribute) {
        List<String> values = SpringRequestMappingSearch.readStrings(annotation, attribute);
        if (values.isEmpty()) {
            return null;
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(client);
        return SpringConfigValues.resolvePlaceholders(module, values.get(0));
    }

    @RequiredReadAction
    private static @Nullable PsiAnnotation findAnnotation(PsiClass psiClass, String qualifiedName) {
        for (PsiAnnotation annotation : SpringRequestMappingSearch.getAnnotations(psiClass)) {
            if (qualifiedName.equals(annotation.getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static void addClient(PsiClass psiClass, Map<String, PsiClass> result) {
        if (!psiClass.isInterface() || psiClass.isAnnotationType()) {
            return;
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName != null) {
            result.putIfAbsent(qualifiedName, psiClass);
        }
    }

    private static List<SpringClientEndpoint> wrap(List<SpringRequestMapping> mappings, SpringClientBaseUrl baseUrl) {
        List<SpringClientEndpoint> result = new ArrayList<>(mappings.size());
        for (SpringRequestMapping mapping : mappings) {
            result.add(new SpringClientEndpoint(mapping, baseUrl));
        }
        return result;
    }
}
