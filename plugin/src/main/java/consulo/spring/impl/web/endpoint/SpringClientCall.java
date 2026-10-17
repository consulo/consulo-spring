package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.JavaResolveResult;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiExpressionList;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiMethodCallExpression;
import com.intellij.java.language.psi.PsiReferenceExpression;
import consulo.annotation.access.RequiredReadAction;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringClientCall {
    private static final String MOCK_MVC_BUILDERS = "org.springframework.test.web.servlet.request.MockMvcRequestBuilders";
    private static final String SPRING_PREFIX = "org.springframework.";
    private static final String URI = "uri";
    private static final String REQUEST = "request";
    private static final String METHOD = "method";

    private static final Map<String, String> MOCK_MVC_METHODS = Map.of(
        "get", "GET",
        "post", "POST",
        "put", "PUT",
        "patch", "PATCH",
        "delete", "DELETE",
        "head", "HEAD",
        "options", "OPTIONS",
        "multipart", "POST"
    );

    private static final Map<String, String> REST_TEMPLATE_METHODS = Map.ofEntries(
        Map.entry("getForObject", "GET"),
        Map.entry("getForEntity", "GET"),
        Map.entry("postForObject", "POST"),
        Map.entry("postForEntity", "POST"),
        Map.entry("postForLocation", "POST"),
        Map.entry("patchForObject", "PATCH"),
        Map.entry("put", "PUT"),
        Map.entry("delete", "DELETE"),
        Map.entry("headForHeaders", "HEAD"),
        Map.entry("optionsForAllow", "OPTIONS"),
        Map.entry("exchange", ""),
        Map.entry("execute", "")
    );

    private static final Set<String> CANDIDATE_NAMES = new HashSet<>();

    static {
        CANDIDATE_NAMES.addAll(MOCK_MVC_METHODS.keySet());
        CANDIDATE_NAMES.addAll(REST_TEMPLATE_METHODS.keySet());
        CANDIDATE_NAMES.add(URI);
        CANDIDATE_NAMES.add(REQUEST);
    }

    private final @Nullable String myHttpMethod;

    private SpringClientCall(@Nullable String httpMethod) {
        myHttpMethod = httpMethod;
    }

    public @Nullable String getHttpMethod() {
        return myHttpMethod;
    }

    @RequiredReadAction
    public static @Nullable PsiMethodCallExpression findCallCandidate(PsiExpression urlExpression) {
        if (!(urlExpression.getParent() instanceof PsiExpressionList list)) {
            return null;
        }
        if (!(list.getParent() instanceof PsiMethodCallExpression call)) {
            return null;
        }
        String name = call.getMethodExpression().getReferenceName();
        return name != null && CANDIDATE_NAMES.contains(name) ? call : null;
    }

    @RequiredReadAction
    public static @Nullable SpringClientCall find(PsiExpression urlExpression) {
        PsiMethodCallExpression call = findCallCandidate(urlExpression);
        if (call == null) {
            return null;
        }
        PsiExpression[] arguments = call.getArgumentList().getExpressions();
        int index = indexOf(arguments, urlExpression);
        String name = call.getMethodExpression().getReferenceName();
        if (index < 0 || name == null) {
            return null;
        }

        PsiMethod method = resolveCandidate(call);
        PsiClass containingClass = method == null ? null : method.getContainingClass();
        String qualifiedName = containingClass == null ? null : containingClass.getQualifiedName();
        if (qualifiedName == null || !qualifiedName.startsWith(SPRING_PREFIX)) {
            return null;
        }

        if (URI.equals(name)) {
            return index == 0 ? new SpringClientCall(getVerbFromReceiver(call)) : null;
        }

        if (MOCK_MVC_BUILDERS.equals(qualifiedName)) {
            if (REQUEST.equals(name)) {
                return index == 1 ? new SpringClientCall(getEnumName(arguments[0])) : null;
            }
            String verb = MOCK_MVC_METHODS.get(name);
            return verb != null && index == 0 ? new SpringClientCall(verb) : null;
        }

        if (qualifiedName.contains("RestTemplate") || qualifiedName.contains("RestOperations")) {
            String verb = REST_TEMPLATE_METHODS.get(name);
            if (verb == null || index != 0) {
                return null;
            }
            if (verb.isEmpty()) {
                return new SpringClientCall(arguments.length > 1 ? getEnumName(arguments[1]) : null);
            }
            return new SpringClientCall(verb);
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable PsiMethod resolveCandidate(PsiMethodCallExpression call) {
        PsiMethod method = call.resolveMethod();
        if (method != null) {
            return method;
        }
        for (JavaResolveResult result : call.getMethodExpression().multiResolve(false)) {
            if (result.getElement() instanceof PsiMethod candidate) {
                return candidate;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static @Nullable String getVerbFromReceiver(PsiMethodCallExpression call) {
        if (!(call.getMethodExpression().getQualifierExpression() instanceof PsiMethodCallExpression receiver)) {
            return null;
        }
        String name = receiver.getMethodExpression().getReferenceName();
        if (name == null) {
            return null;
        }
        if (METHOD.equals(name)) {
            PsiExpression[] arguments = receiver.getArgumentList().getExpressions();
            return arguments.length > 0 ? getEnumName(arguments[0]) : null;
        }
        String verb = MOCK_MVC_METHODS.get(name);
        return verb != null && !"multipart".equals(name) ? verb : null;
    }

    @RequiredReadAction
    private static @Nullable String getEnumName(PsiExpression expression) {
        if (expression instanceof PsiReferenceExpression reference) {
            String name = reference.getReferenceName();
            return name == null ? null : name.toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private static int indexOf(PsiExpression[] arguments, PsiExpression expression) {
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == expression) {
                return i;
            }
        }
        return -1;
    }
}
