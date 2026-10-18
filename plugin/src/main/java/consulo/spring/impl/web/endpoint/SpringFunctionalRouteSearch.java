package consulo.spring.impl.web.endpoint;

import com.intellij.java.indexing.search.searches.AnnotatedElementsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiCodeBlock;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiExpressionStatement;
import com.intellij.java.language.psi.PsiLambdaExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiMethodCallExpression;
import com.intellij.java.language.psi.PsiMethodReferenceExpression;
import com.intellij.java.language.psi.PsiReferenceExpression;
import com.intellij.java.language.psi.PsiReturnStatement;
import com.intellij.java.language.psi.PsiStatement;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringFunctionalRouteSearch {
    private static final int MAX_DEPTH = 8;
    private static final int MAX_ROUTES = 100;

    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
    private static final Set<String> NEUTRAL_PREDICATES = Set.of("accept", "contentType", "headers", "queryParam", "param", "all", "pathExtension", "version");

    private SpringFunctionalRouteSearch() {
    }

    @RequiredReadAction
    public static List<PsiClass> findRouterClasses(Project project, GlobalSearchScope scope, String routerFunctionType) {
        if (DumbService.isDumb(project)) {
            return List.of();
        }
        PsiClass bean = JavaPsiFacade.getInstance(project).findClass(SpringWebConstants.BEAN, GlobalSearchScope.allScope(project));
        if (bean == null) {
            return List.of();
        }
        Map<String, PsiClass> result = new TreeMap<>();
        AnnotatedElementsSearch.searchPsiMethods(bean, scope).forEach(method -> {
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null && isRouterMethod(method, routerFunctionType)) {
                String qualifiedName = containingClass.getQualifiedName();
                if (qualifiedName != null) {
                    result.putIfAbsent(qualifiedName, containingClass);
                }
            }
            return true;
        });
        return List.copyOf(result.values());
    }

    @RequiredReadAction
    public static List<SpringRequestMapping> findRoutes(PsiClass psiClass, String routerFunctionType) {
        Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
        List<SpringRequestMapping> result = new ArrayList<>();
        for (PsiMethod method : psiClass.getMethods()) {
            if (!isRouterMethod(method, routerFunctionType) || method.getModifierList().findAnnotation(SpringWebConstants.BEAN) == null) {
                continue;
            }
            PsiCodeBlock body = method.getBody();
            if (body == null) {
                continue;
            }
            for (PsiReturnStatement statement : PsiTreeUtil.findChildrenOfType(body, PsiReturnStatement.class)) {
                if (PsiTreeUtil.getParentOfType(statement, PsiLambdaExpression.class, true, PsiMethod.class) != null) {
                    continue;
                }
                PsiExpression value = statement.getReturnValue();
                if (value != null) {
                    router(value, "", 0, method, module, result);
                }
            }
        }
        return result;
    }

    @RequiredReadAction
    public static boolean isRouterMethod(PsiMethod method, String routerFunctionType) {
        PsiType returnType = method.getReturnType();
        if (!(returnType instanceof PsiClassType classType)) {
            return false;
        }
        PsiClass psiClass = classType.resolve();
        String name = psiClass != null ? psiClass.getQualifiedName() : classType.rawType().getCanonicalText();
        return routerFunctionType.equals(name);
    }

    @RequiredReadAction
    private static void router(
        @Nullable PsiExpression expression,
        String prefix,
        int depth,
        PsiMethod routerMethod,
        @Nullable Module module,
        List<SpringRequestMapping> result
    ) {
        PsiExpression current = PsiUtil.skipParenthesizedExprDown(expression);
        if (depth > MAX_DEPTH || result.size() >= MAX_ROUTES || !(current instanceof PsiMethodCallExpression call)) {
            return;
        }
        for (PsiMethodCallExpression element : chain(call)) {
            String name = element.getMethodExpression().getReferenceName();
            PsiExpression[] arguments = element.getArgumentList().getExpressions();
            if (name == null) {
                continue;
            }
            if (HTTP_METHODS.contains(name)) {
                builderRoute(element, name, arguments, prefix, routerMethod, module, result);
            }
            else if (("route".equals(name) || "andRoute".equals(name)) && arguments.length == 2) {
                List<SpringRoutePredicate> predicates = predicate(arguments[0], 0);
                if (predicates != null) {
                    for (SpringRoutePredicate predicate : predicates) {
                        add(element, predicate.getHttpMethod(), prefix, predicate.getPath(), arguments[1], routerMethod, module, result);
                    }
                }
            }
            else if ("path".equals(name) && arguments.length == 2) {
                String path = SpringRequestMappingSearch.evaluateText(arguments[0]);
                if (path != null) {
                    nested(arguments[1], prefix + path, depth + 1, routerMethod, module, result);
                }
            }
            else if (("nest".equals(name) || "andNest".equals(name)) && arguments.length == 2) {
                List<SpringRoutePredicate> predicates = predicate(arguments[0], 0);
                if (predicates != null) {
                    for (SpringRoutePredicate predicate : predicates) {
                        nested(arguments[1], prefix + predicate.getPath(), depth + 1, routerMethod, module, result);
                    }
                }
            }
            else if (("add".equals(name) || "and".equals(name) || "andOther".equals(name)) && arguments.length == 1) {
                router(arguments[0], prefix, depth + 1, routerMethod, module, result);
            }
        }
    }

    @RequiredReadAction
    private static void builderRoute(
        PsiMethodCallExpression call,
        String httpMethod,
        PsiExpression[] arguments,
        String prefix,
        PsiMethod routerMethod,
        @Nullable Module module,
        List<SpringRequestMapping> result
    ) {
        if (arguments.length == 0) {
            return;
        }
        PsiExpression handler = arguments[arguments.length - 1];
        if (arguments.length == 1) {
            add(call, httpMethod, prefix, "", handler, routerMethod, module, result);
            return;
        }
        String path = SpringRequestMappingSearch.evaluateText(arguments[0]);
        if (path != null) {
            add(call, httpMethod, prefix, path, handler, routerMethod, module, result);
            return;
        }
        List<SpringRoutePredicate> predicates = predicate(arguments[0], 0);
        if (predicates == null) {
            return;
        }
        for (SpringRoutePredicate predicate : predicates) {
            String predicateMethod = predicate.getHttpMethod();
            if (predicateMethod == null || predicateMethod.equals(httpMethod)) {
                add(call, httpMethod, prefix, predicate.getPath(), handler, routerMethod, module, result);
            }
        }
    }

    @RequiredReadAction
    private static void nested(
        PsiExpression argument,
        String prefix,
        int depth,
        PsiMethod routerMethod,
        @Nullable Module module,
        List<SpringRequestMapping> result
    ) {
        PsiExpression current = PsiUtil.skipParenthesizedExprDown(argument);
        if (!(current instanceof PsiLambdaExpression lambda)) {
            router(current, prefix, depth, routerMethod, module, result);
            return;
        }
        PsiElement body = lambda.getBody();
        if (body instanceof PsiExpression expression) {
            router(expression, prefix, depth, routerMethod, module, result);
        }
        else if (body instanceof PsiCodeBlock block) {
            for (PsiStatement statement : block.getStatements()) {
                if (statement instanceof PsiExpressionStatement expressionStatement) {
                    router(expressionStatement.getExpression(), prefix, depth, routerMethod, module, result);
                }
                else if (statement instanceof PsiReturnStatement returnStatement) {
                    router(returnStatement.getReturnValue(), prefix, depth, routerMethod, module, result);
                }
            }
        }
    }

    @RequiredReadAction
    private static @Nullable List<SpringRoutePredicate> predicate(@Nullable PsiExpression expression, int depth) {
        PsiExpression current = PsiUtil.skipParenthesizedExprDown(expression);
        if (depth > MAX_DEPTH || !(current instanceof PsiMethodCallExpression call)) {
            return null;
        }
        String name = call.getMethodExpression().getReferenceName();
        PsiExpression[] arguments = call.getArgumentList().getExpressions();
        PsiExpression qualifier = call.getMethodExpression().getQualifierExpression();
        if (name == null) {
            return null;
        }
        if (("and".equals(name) || "or".equals(name)) && arguments.length == 1 && qualifier instanceof PsiMethodCallExpression) {
            List<SpringRoutePredicate> left = predicate(qualifier, depth + 1);
            List<SpringRoutePredicate> right = predicate(arguments[0], depth + 1);
            if (left == null || right == null) {
                return null;
            }
            List<SpringRoutePredicate> result = new ArrayList<>();
            if ("or".equals(name)) {
                result.addAll(left);
                result.addAll(right);
                return result;
            }
            for (SpringRoutePredicate l : left) {
                for (SpringRoutePredicate r : right) {
                    SpringRoutePredicate combined = l.and(r);
                    if (combined != null) {
                        result.add(combined);
                    }
                }
            }
            return result;
        }
        if (HTTP_METHODS.contains(name) && arguments.length == 1) {
            String path = SpringRequestMappingSearch.evaluateText(arguments[0]);
            return path == null ? null : List.of(new SpringRoutePredicate(name, path));
        }
        if ("path".equals(name) && arguments.length == 1) {
            String path = SpringRequestMappingSearch.evaluateText(arguments[0]);
            return path == null ? null : List.of(new SpringRoutePredicate(null, path));
        }
        if ("method".equals(name) && arguments.length == 1 && PsiUtil.skipParenthesizedExprDown(arguments[0]) instanceof PsiReferenceExpression reference) {
            String httpMethod = reference.getReferenceName();
            return httpMethod == null ? null : List.of(new SpringRoutePredicate(httpMethod.toUpperCase(Locale.ROOT), ""));
        }
        if (NEUTRAL_PREDICATES.contains(name)) {
            return List.of(SpringRoutePredicate.ANY);
        }
        return null;
    }

    @RequiredReadAction
    private static void add(
        PsiMethodCallExpression call,
        @Nullable String httpMethod,
        String prefix,
        String path,
        PsiExpression handler,
        PsiMethod routerMethod,
        @Nullable Module module,
        List<SpringRequestMapping> result
    ) {
        if (result.size() >= MAX_ROUTES) {
            return;
        }
        String fullPath = SpringRequestMappingSearch.collapseSlashes(
            SpringConfigValues.resolvePlaceholders(module, SpringRequestMappingSearch.join(prefix, path))
        );
        PsiMethod handlerMethod = resolveHandler(handler);
        PsiElement nameElement = call.getMethodExpression().getReferenceNameElement();
        PsiElement declaration = nameElement != null ? nameElement : call;
        result.add(SpringRequestMapping.route(handlerMethod != null ? handlerMethod : routerMethod, declaration, fullPath, httpMethod));
    }

    @RequiredReadAction
    private static @Nullable PsiMethod resolveHandler(PsiExpression handler) {
        PsiExpression current = PsiUtil.skipParenthesizedExprDown(handler);
        if (current instanceof PsiMethodReferenceExpression reference && reference.resolve() instanceof PsiMethod method) {
            return method;
        }
        return null;
    }

    private static List<PsiMethodCallExpression> chain(PsiMethodCallExpression call) {
        Deque<PsiMethodCallExpression> result = new ArrayDeque<>();
        PsiExpression current = call;
        while (current instanceof PsiMethodCallExpression element) {
            result.addFirst(element);
            current = PsiUtil.skipParenthesizedExprDown(element.getMethodExpression().getQualifierExpression());
        }
        return List.copyOf(result);
    }
}
