package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.UrlPath;
import consulo.endpoint.url.inlay.PsiElementUrlPathInlayHint;
import consulo.endpoint.url.inlay.UrlPathInlayHint;
import consulo.endpoint.url.inlay.UrlPathInlayHintsProviderSemElement;
import consulo.endpoint.url.reference.UrlPathContext;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringClientUrlPathInlaySemElement implements UrlPathInlayHintsProviderSemElement {
    private final PsiAnnotation myAnnotation;
    private final PsiMethod myMethod;

    private SpringClientUrlPathInlaySemElement(PsiAnnotation annotation, PsiMethod method) {
        myAnnotation = annotation;
        myMethod = method;
    }

    @RequiredReadAction
    public static @Nullable SpringClientUrlPathInlaySemElement create(PsiAnnotation annotation) {
        PsiMethod method = SpringRequestMappingSearch.getOwnerMethod(annotation);
        if (method == null) {
            return null;
        }
        boolean client = SpringClientSearch.isExchangeAnnotation(annotation)
            || SpringClientSearch.isFeignClient(method.getContainingClass()) && SpringRequestMappingSearch.isMappingAnnotation(annotation);
        if (!client || !SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(annotation))) {
            return null;
        }
        return new SpringClientUrlPathInlaySemElement(annotation, method);
    }

    @Override
    @RequiredReadAction
    public List<UrlPathInlayHint> getInlayHints() {
        if (!myAnnotation.isValid() || !myMethod.isValid()) {
            return List.of();
        }
        List<SpringClientEndpoint> endpoints = new ArrayList<>();
        for (SpringClientEndpoint endpoint : SpringClientSearch.findEndpoints(myMethod)) {
            if (endpoint.getMapping().getAnnotation() == myAnnotation) {
                endpoints.add(endpoint);
            }
        }
        if (endpoints.isEmpty()) {
            return List.of();
        }

        List<UrlPath> paths = new ArrayList<>();
        for (SpringClientEndpoint endpoint : endpoints) {
            paths.add(SpringClientUrlTargetInfo.create(endpoint).getPath());
        }
        List<PsiLiteralExpression> literals = SpringRequestMappingSearch.findPathLiterals(myAnnotation);
        PsiElement anchor = literals.isEmpty() ? myAnnotation : literals.get(0);
        UrlPathContext context = new UrlPathContext(SpringClientUrlTargetInfo.create(endpoints.get(0))).withPaths(paths);
        return List.of(new PsiElementUrlPathInlayHint(anchor, context));
    }
}
