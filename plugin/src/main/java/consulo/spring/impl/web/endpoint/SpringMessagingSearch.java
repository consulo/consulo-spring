package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiCodeBlock;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiMethodCallExpression;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.search.PsiShortNamesCache;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
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
public final class SpringMessagingSearch {
    public static final String SEND = "SEND";
    public static final String SUBSCRIBE = "SUBSCRIBE";

    private static final String[] DESTINATION_ATTRIBUTES = {"value"};
    private static final String PREFIXES_METHOD = "setApplicationDestinationPrefixes";
    private static final String CONFIGURE_METHOD = "configureMessageBroker";
    private static final int MAX_CONFIGURERS = 50;

    private SpringMessagingSearch() {
    }

    @RequiredReadAction
    public static boolean isAvailable(@Nullable Module module) {
        return SpringModuleClasses.has(module, SpringWebConstants.MESSAGE_MAPPING);
    }

    @RequiredReadAction
    public static List<PsiClass> findControllers(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        Map<String, PsiClass> result = new TreeMap<>();
        for (String annotationName : List.of(SpringWebConstants.MESSAGE_MAPPING, SpringWebConstants.SUBSCRIBE_MAPPING)) {
            PsiClass annotationClass = facade.findClass(annotationName, allScope);
            if (annotationClass == null) {
                continue;
            }
            AnnotatedElementsSearch.searchPsiMethods(annotationClass, scope).forEach(method -> {
                PsiClass containingClass = method.getContainingClass();
                if (containingClass != null && !containingClass.isInterface()) {
                    String qualifiedName = containingClass.getQualifiedName();
                    if (qualifiedName != null) {
                        result.putIfAbsent(qualifiedName, containingClass);
                    }
                }
                return true;
            });
        }
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findDestinations(PsiClass controller) {
        List<String> applicationPrefixes = getApplicationPrefixes(controller.getProject());
        List<String> classPrefixes = readDestinations(controller.getModifierList(), SpringWebConstants.MESSAGE_MAPPING, List.of(""));

        List<SpringRequestMapping> result = new ArrayList<>();
        for (PsiMethod method : controller.getMethods()) {
            PsiElement nameIdentifier = method.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            addDestinations(result, method, nameIdentifier, SpringWebConstants.MESSAGE_MAPPING, SEND, applicationPrefixes, classPrefixes);
            addDestinations(result, method, nameIdentifier, SpringWebConstants.SUBSCRIBE_MAPPING, SUBSCRIBE, applicationPrefixes, classPrefixes);
        }
        return result;
    }

    @RequiredReadAction
    private static void addDestinations(
        List<SpringRequestMapping> result,
        PsiMethod method,
        PsiElement nameIdentifier,
        String annotationName,
        String kind,
        List<String> applicationPrefixes,
        List<String> classPrefixes
    ) {
        if (method.getModifierList().findAnnotation(annotationName) == null) {
            return;
        }
        for (String destination : readDestinations(method.getModifierList(), annotationName, List.of(""))) {
            for (String applicationPrefix : applicationPrefixes) {
                for (String classPrefix : classPrefixes) {
                    String path = SpringRequestMappingSearch.join(SpringRequestMappingSearch.join(applicationPrefix, classPrefix), destination);
                    result.add(SpringRequestMapping.route(method, nameIdentifier, SpringRequestMappingSearch.collapseSlashes(path), kind));
                }
            }
        }
    }

    @RequiredReadAction
    private static List<String> readDestinations(@Nullable PsiModifierList modifierList, String annotationName, List<String> defaults) {
        PsiAnnotation annotation = modifierList == null ? null : modifierList.findAnnotation(annotationName);
        if (annotation == null) {
            return defaults;
        }
        return SpringRequestMappingSearch.readPaths(annotation, DESTINATION_ATTRIBUTES, defaults);
    }

    @RequiredReadAction
    private static List<String> getApplicationPrefixes(Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> CachedValueProvider.Result.create(
            computeApplicationPrefixes(project),
            SpringWebModificationTracker.getInstance(project)
        ));
    }

    @RequiredReadAction
    private static List<String> computeApplicationPrefixes(Project project) {
        if (DumbService.isDumb(project)) {
            return List.of("");
        }
        Set<String> result = new LinkedHashSet<>();
        PsiMethod[] configurers = PsiShortNamesCache.getInstance(project)
            .getMethodsByNameIfNotMoreThan(CONFIGURE_METHOD, GlobalSearchScope.projectScope(project), MAX_CONFIGURERS);
        for (PsiMethod configurer : configurers) {
            PsiCodeBlock body = configurer.getBody();
            if (body == null) {
                continue;
            }
            for (PsiMethodCallExpression call : PsiTreeUtil.findChildrenOfType(body, PsiMethodCallExpression.class)) {
                if (!PREFIXES_METHOD.equals(call.getMethodExpression().getReferenceName())) {
                    continue;
                }
                for (PsiExpression argument : call.getArgumentList().getExpressions()) {
                    String prefix = SpringRequestMappingSearch.evaluateText(argument);
                    if (prefix != null) {
                        result.add(prefix);
                    }
                }
            }
        }
        return result.isEmpty() ? List.of("") : List.copyOf(result);
    }
}
