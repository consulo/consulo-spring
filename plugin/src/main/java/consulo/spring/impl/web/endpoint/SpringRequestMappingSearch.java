package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringRequestMappingSearch {
    private static final String[] PATH_ATTRIBUTES = {"path", "value"};
    private static final String[] PATH_LITERAL_ATTRIBUTES = {"path", "value", "url"};

    private SpringRequestMappingSearch() {
    }

    @RequiredReadAction
    public static List<PsiClass> findControllers(Project project, GlobalSearchScope scope) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }

        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        PsiClass controller = facade.findClass(SpringWebConstants.CONTROLLER, allScope);
        if (controller == null) {
            return List.of();
        }

        Set<PsiClass> stereotypes = new LinkedHashSet<>();
        collectAnnotationTypes(controller, allScope, stereotypes);

        Map<String, PsiClass> result = new TreeMap<>();
        for (PsiClass stereotype : stereotypes) {
            AnnotatedElementsSearch.searchPsiClasses(stereotype, scope).forEach(psiClass -> {
                if (psiClass.isAnnotationType() || psiClass.isInterface()) {
                    return true;
                }
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null) {
                    result.putIfAbsent(qualifiedName, psiClass);
                }
                return true;
            });
        }
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findMappings(PsiClass controller) {
        return findMappings(controller, findClassPaths(controller), SpringRequestMappingSearch::resolveMappingInfo);
    }

    @RequiredReadAction
    static List<SpringRequestMapping> findMappings(
        PsiClass owner,
        List<String> prefixes,
        Function<PsiAnnotation, @Nullable SpringMappingInfo> resolver
    ) {
        Module module = ModuleUtilCore.findModuleForPsiElement(owner);

        List<SpringRequestMapping> result = new ArrayList<>();
        Set<PsiMethod> visited = new HashSet<>();
        for (PsiMethod method : owner.getMethods()) {
            visited.add(method);
            collectMethodMappings(method, method, prefixes, module, resolver, result);
        }

        for (PsiClass superClass : collectSupers(owner)) {
            for (PsiMethod superMethod : superClass.getMethods()) {
                if (visited.contains(superMethod)) {
                    continue;
                }
                PsiMethod visible = owner.findMethodBySignature(superMethod, true);
                if (visible != superMethod) {
                    continue;
                }
                visited.add(superMethod);
                collectMethodMappings(superMethod, superMethod, prefixes, module, resolver, result);
            }
        }
        return result;
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findMappings(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return List.of();
        }
        return findMappings(method, findClassPaths(containingClass), SpringRequestMappingSearch::resolveMappingInfo);
    }

    @RequiredReadAction
    static List<SpringRequestMapping> findMappings(
        PsiMethod method,
        List<String> prefixes,
        Function<PsiAnnotation, @Nullable SpringMappingInfo> resolver
    ) {
        List<SpringRequestMapping> result = new ArrayList<>();
        collectMethodMappings(method, method, prefixes, ModuleUtilCore.findModuleForPsiElement(method), resolver, result);
        return result;
    }

    @RequiredReadAction
    public static boolean isMappingAnnotation(PsiAnnotation annotation) {
        return resolveMappingInfo(annotation) != null;
    }

    @RequiredReadAction
    public static boolean isPathAnnotation(PsiAnnotation annotation) {
        return isMappingAnnotation(annotation) || SpringClientSearch.isExchangeAnnotation(annotation);
    }

    @RequiredReadAction
    public static List<PsiLiteralExpression> findPathLiterals(PsiAnnotation annotation) {
        List<PsiLiteralExpression> result = new ArrayList<>();
        for (String attribute : PATH_LITERAL_ATTRIBUTES) {
            PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attribute);
            for (PsiAnnotationMemberValue item : AnnotationUtil.arrayAttributeValues(value)) {
                if (item instanceof PsiLiteralExpression literal && literal.getValue() instanceof String) {
                    result.add(literal);
                }
            }
        }
        return result;
    }

    @RequiredReadAction
    public static List<String> readStrings(PsiAnnotation annotation, String attribute) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attribute);
        List<String> result = new ArrayList<>();
        for (PsiAnnotationMemberValue item : AnnotationUtil.arrayAttributeValues(value)) {
            String text = evaluateString(item);
            if (text != null && !text.isBlank()) {
                result.add(text);
            }
        }
        return result;
    }

    @RequiredReadAction
    public static List<SpringQueryParameter> findQueryParameters(PsiMethod method) {
        List<SpringQueryParameter> result = new ArrayList<>();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiAnnotation annotation = findParameterAnnotation(parameter, SpringWebConstants.REQUEST_PARAM);
            if (annotation == null) {
                continue;
            }
            String name = getParameterBindingName(parameter, annotation);
            if (name == null) {
                continue;
            }
            result.add(new SpringQueryParameter(name, isRepeatable(parameter.getType()), parameter));
        }
        return result;
    }

    @RequiredReadAction
    public static @Nullable PsiAnnotation findParameterAnnotation(PsiParameter parameter, String qualifiedName) {
        PsiModifierList modifierList = parameter.getModifierList();
        if (modifierList == null) {
            return null;
        }
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (qualifiedName.equals(annotation.getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }

    @RequiredReadAction
    public static @Nullable String getExplicitBindingName(PsiAnnotation annotation) {
        for (String attribute : new String[]{"value", "name"}) {
            PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attribute);
            if (value != null) {
                String text = evaluateString(value);
                if (text != null && !text.isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }

    @RequiredReadAction
    public static @Nullable String getParameterBindingName(PsiParameter parameter, PsiAnnotation annotation) {
        String explicit = getExplicitBindingName(annotation);
        if (explicit != null) {
            return explicit;
        }
        if (isMapType(parameter.getType())) {
            return null;
        }
        return parameter.getName();
    }

    @RequiredReadAction
    public static List<PsiLiteralExpression> findDeclaredPathLiterals(PsiMethod method) {
        List<PsiLiteralExpression> result = new ArrayList<>();
        for (PsiAnnotation annotation : method.getModifierList().getAnnotations()) {
            if (isPathAnnotation(annotation)) {
                result.addAll(findPathLiterals(annotation));
            }
        }
        PsiClass containingClass = method.getContainingClass();
        if (containingClass != null) {
            for (PsiClass psiClass : withSupers(containingClass)) {
                for (PsiAnnotation annotation : getAnnotations(psiClass)) {
                    if (isPathAnnotation(annotation)) {
                        result.addAll(findPathLiterals(annotation));
                    }
                }
            }
        }
        return result;
    }

    private static boolean isRepeatable(PsiType type) {
        if (type instanceof PsiArrayType) {
            return true;
        }
        String text = type.getCanonicalText();
        return text.startsWith("java.util.List") || text.startsWith("java.util.Set") || text.startsWith("java.util.Collection");
    }

    private static boolean isMapType(PsiType type) {
        String text = type.getCanonicalText();
        return text.startsWith("java.util.Map") || text.startsWith("org.springframework.util.MultiValueMap");
    }

    public static String join(String prefix, String path) {
        String head = normalizePrefix(prefix);
        String tail = path.isEmpty() || path.startsWith("/") ? path : "/" + path;
        String joined = head + tail;
        return joined.isEmpty() ? "/" : joined;
    }

    @RequiredReadAction
    private static void collectMethodMappings(
        PsiMethod method,
        PsiMethod annotationOwner,
        List<String> prefixes,
        @Nullable Module module,
        Function<PsiAnnotation, @Nullable SpringMappingInfo> resolver,
        List<SpringRequestMapping> result
    ) {
        PsiAnnotation[] annotations = annotationOwner.getModifierList().getAnnotations();
        boolean found = false;
        for (PsiAnnotation annotation : annotations) {
            SpringMappingInfo info = resolver.apply(annotation);
            if (info == null) {
                continue;
            }
            found = true;
            for (String prefix : prefixes) {
                for (String path : info.paths()) {
                    String fullPath = collapseSlashes(SpringConfigValues.resolvePlaceholders(module, join(prefix, path)));
                    if (info.methods().isEmpty()) {
                        result.add(new SpringRequestMapping(method, annotation, fullPath, null));
                    }
                    else {
                        for (String httpMethod : info.methods()) {
                            result.add(new SpringRequestMapping(method, annotation, fullPath, httpMethod));
                        }
                    }
                }
            }
        }

        if (!found && annotationOwner == method) {
            for (PsiMethod superMethod : method.findSuperMethods()) {
                int before = result.size();
                collectMethodMappings(method, superMethod, prefixes, module, resolver, result);
                if (result.size() != before) {
                    return;
                }
            }
        }
    }

    @RequiredReadAction
    private static List<String> findClassPaths(PsiClass controller) {
        for (PsiClass psiClass : withSupers(controller)) {
            for (PsiAnnotation annotation : getAnnotations(psiClass)) {
                SpringMappingInfo info = resolveMappingInfo(annotation);
                if (info != null) {
                    return info.paths();
                }
            }
        }
        return List.of("");
    }

    @RequiredReadAction
    static @Nullable SpringMappingInfo resolveMappingInfo(PsiAnnotation annotation) {
        String qualifiedName = annotation.getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }

        if (SpringWebConstants.REQUEST_MAPPING.equals(qualifiedName)) {
            return new SpringMappingInfo(readPaths(annotation, List.of("")), readMethods(annotation));
        }

        String composedMethod = SpringWebConstants.COMPOSED_MAPPINGS.get(qualifiedName);
        if (composedMethod != null) {
            return new SpringMappingInfo(readPaths(annotation, List.of("")), List.of(composedMethod));
        }

        PsiClass annotationClass = annotation.resolveAnnotationType();
        if (annotationClass == null) {
            return null;
        }
        PsiAnnotation meta = findMetaMapping(annotationClass, new HashSet<>());
        if (meta == null) {
            return null;
        }
        SpringMappingInfo metaInfo = resolveMappingInfo(meta);
        if (metaInfo == null) {
            return null;
        }
        return new SpringMappingInfo(readPaths(annotation, metaInfo.paths()), metaInfo.methods());
    }

    @RequiredReadAction
    private static @Nullable PsiAnnotation findMetaMapping(PsiClass annotationClass, Set<PsiClass> visited) {
        if (!visited.add(annotationClass)) {
            return null;
        }
        for (PsiAnnotation annotation : getAnnotations(annotationClass)) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null || qualifiedName.startsWith("java.lang.annotation.")) {
                continue;
            }
            if (SpringWebConstants.REQUEST_MAPPING.equals(qualifiedName) || SpringWebConstants.COMPOSED_MAPPINGS.containsKey(qualifiedName)) {
                return annotation;
            }
            PsiClass type = annotation.resolveAnnotationType();
            if (type != null && findMetaMapping(type, visited) != null) {
                return annotation;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static List<String> readPaths(PsiAnnotation annotation, List<String> defaultPaths) {
        return readPaths(annotation, PATH_ATTRIBUTES, defaultPaths);
    }

    @RequiredReadAction
    static List<String> readPaths(PsiAnnotation annotation, String[] attributes, List<String> defaultPaths) {
        List<String> paths = new ArrayList<>();
        for (String attribute : attributes) {
            PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attribute);
            if (value == null) {
                continue;
            }
            for (PsiAnnotationMemberValue item : AnnotationUtil.arrayAttributeValues(value)) {
                String text = evaluateString(item);
                if (text != null) {
                    paths.add(text);
                }
            }
            if (!paths.isEmpty()) {
                return paths;
            }
        }
        return defaultPaths;
    }

    @RequiredReadAction
    private static List<String> readMethods(PsiAnnotation annotation) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("method");
        if (value == null) {
            return List.of();
        }
        Set<String> methods = new LinkedHashSet<>();
        for (PsiAnnotationMemberValue item : AnnotationUtil.arrayAttributeValues(value)) {
            if (item instanceof PsiReferenceExpression reference) {
                String name = reference.getReferenceName();
                if (name != null) {
                    methods.add(name);
                }
            }
        }
        return List.copyOf(methods);
    }

    @RequiredReadAction
    private static @Nullable String evaluateString(PsiAnnotationMemberValue value) {
        return evaluateString(value, 0);
    }

    @RequiredReadAction
    private static @Nullable String evaluateString(@Nullable PsiElement value, int depth) {
        if (value == null || depth > 10) {
            return null;
        }
        if (value instanceof PsiLiteralExpression literal) {
            return literal.getValue() instanceof String text ? text : null;
        }
        if (value instanceof PsiParenthesizedExpression parenthesized) {
            return evaluateString(parenthesized.getExpression(), depth + 1);
        }
        if (value instanceof PsiPolyadicExpression polyadic && polyadic.getOperationTokenType() == JavaTokenType.PLUS) {
            StringBuilder builder = new StringBuilder();
            for (PsiExpression operand : polyadic.getOperands()) {
                String text = evaluateString(operand, depth + 1);
                if (text == null) {
                    return computeConstant(polyadic);
                }
                builder.append(text);
            }
            return builder.toString();
        }
        if (value instanceof PsiReferenceExpression reference
            && reference.resolve() instanceof PsiField field
            && field.hasModifierProperty(PsiModifier.FINAL)) {
            String text = evaluateString(field.getInitializer(), depth + 1);
            if (text != null) {
                return text;
            }
        }
        return value instanceof PsiExpression expression ? computeConstant(expression) : null;
    }

    @RequiredReadAction
    private static @Nullable String computeConstant(PsiExpression expression) {
        Object constant = JavaPsiFacade.getInstance(expression.getProject()).getConstantEvaluationHelper().computeConstantExpression(expression);
        return constant instanceof String text ? text : null;
    }

    @RequiredReadAction
    static PsiAnnotation[] getAnnotations(PsiClass psiClass) {
        PsiModifierList modifierList = psiClass.getModifierList();
        return modifierList == null ? PsiAnnotation.EMPTY_ARRAY : modifierList.getAnnotations();
    }

    @RequiredReadAction
    private static List<PsiClass> withSupers(PsiClass psiClass) {
        List<PsiClass> result = new ArrayList<>();
        result.add(psiClass);
        result.addAll(collectSupers(psiClass));
        return result;
    }

    @RequiredReadAction
    private static List<PsiClass> collectSupers(PsiClass psiClass) {
        Set<PsiClass> result = new LinkedHashSet<>();
        Deque<PsiClass> queue = new ArrayDeque<>(List.of(psiClass.getSupers()));
        while (!queue.isEmpty()) {
            PsiClass next = queue.poll();
            if (CommonClassNames.JAVA_LANG_OBJECT.equals(next.getQualifiedName()) || !result.add(next)) {
                continue;
            }
            queue.addAll(List.of(next.getSupers()));
        }
        return List.copyOf(result);
    }

    @RequiredReadAction
    private static void collectAnnotationTypes(PsiClass annotationType, GlobalSearchScope scope, Set<PsiClass> result) {
        if (!result.add(annotationType)) {
            return;
        }
        AnnotatedElementsSearch.searchPsiClasses(annotationType, scope).forEach(child -> {
            if (child.isAnnotationType()) {
                collectAnnotationTypes(child, scope, result);
            }
            return true;
        });
    }

    private static String collapseSlashes(String path) {
        return path.indexOf("//") < 0 ? path : path.replaceAll("/{2,}", "/");
    }

    private static String normalizePrefix(String prefix) {
        if (prefix.isEmpty() || prefix.equals("/")) {
            return "";
        }
        String result = prefix.startsWith("/") ? prefix : "/" + prefix;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @RequiredReadAction
    static @Nullable PsiMethod getOwnerMethod(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PsiModifierList modifierList && modifierList.getParent() instanceof PsiMethod method) {
            return method;
        }
        return null;
    }
}
