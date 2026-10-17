package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringMappingIndex {
    private static final String[] CONDITION_ATTRIBUTES = {"params", "headers", "consumes", "produces"};

    private SpringMappingIndex() {
    }

    @RequiredReadAction
    public static Map<String, List<SpringRequestMapping>> get(Module module) {
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> CachedValueProvider.Result.create(
            build(module),
            SpringWebModificationTracker.getInstance(module.getProject())
        ));
    }

    @RequiredReadAction
    public static String getKey(SpringRequestMapping mapping) {
        StringBuilder builder = new StringBuilder();
        builder.append(getPresentation(mapping));
        for (String attribute : CONDITION_ATTRIBUTES) {
            TreeSet<String> values = new TreeSet<>(SpringRequestMappingSearch.readStrings(mapping.getAnnotation(), attribute));
            if (!values.isEmpty()) {
                builder.append(' ').append(attribute).append('=').append(values);
            }
        }
        return builder.toString();
    }

    public static String getPresentation(SpringRequestMapping mapping) {
        String httpMethod = mapping.getHttpMethod();
        return (httpMethod == null ? "*" : httpMethod) + " " + SpringUriTemplateUtil.normalize(mapping.getPath());
    }

    @RequiredReadAction
    private static Map<String, List<SpringRequestMapping>> build(Module module) {
        Map<String, List<SpringRequestMapping>> result = new LinkedHashMap<>();
        for (PsiClass controller : SpringRequestMappingSearch.findControllers(module.getProject(), GlobalSearchScope.moduleScope(module))) {
            for (SpringRequestMapping mapping : SpringRequestMappingSearch.findMappings(controller)) {
                result.computeIfAbsent(getKey(mapping), key -> new ArrayList<>()).add(mapping);
            }
        }
        return result;
    }
}
