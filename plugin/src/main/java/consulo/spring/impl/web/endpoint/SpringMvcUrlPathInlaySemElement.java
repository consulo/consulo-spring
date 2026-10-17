package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.UrlPath;
import consulo.endpoint.url.inlay.PsiElementUrlPathInlayHint;
import consulo.endpoint.url.inlay.UrlPathInlayHint;
import consulo.endpoint.url.inlay.UrlPathInlayHintsProviderSemElement;
import consulo.endpoint.url.reference.DefaultFrameworkUrlPathSpecification;
import consulo.endpoint.url.reference.UrlPathContext;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringMvcUrlPathInlaySemElement implements UrlPathInlayHintsProviderSemElement {
    private final PsiAnnotation myAnnotation;
    private final PsiMethod myMethod;

    private SpringMvcUrlPathInlaySemElement(PsiAnnotation annotation, PsiMethod method) {
        myAnnotation = annotation;
        myMethod = method;
    }

    @RequiredReadAction
    public static @Nullable SpringMvcUrlPathInlaySemElement create(PsiAnnotation annotation) {
        PsiMethod method = SpringRequestMappingSearch.getOwnerMethod(annotation);
        if (method == null || !SpringRequestMappingSearch.isMappingAnnotation(annotation)) {
            return null;
        }
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(annotation))) {
            return null;
        }
        return new SpringMvcUrlPathInlaySemElement(annotation, method);
    }

    @Override
    @RequiredReadAction
    public List<UrlPathInlayHint> getInlayHints() {
        if (!myAnnotation.isValid() || !myMethod.isValid()) {
            return List.of();
        }
        List<SpringRequestMapping> mappings = new ArrayList<>();
        for (SpringRequestMapping mapping : SpringRequestMappingSearch.findMappings(myMethod)) {
            if (mapping.getAnnotation() == myAnnotation) {
                mappings.add(mapping);
            }
        }
        if (mappings.isEmpty()) {
            return List.of();
        }

        List<PsiLiteralExpression> literals = SpringRequestMappingSearch.findPathLiterals(myAnnotation);
        PsiElement anchor = literals.isEmpty() ? myAnnotation : literals.get(0);
        UrlPathContext context = new UrlPathContext(SpringMvcUrlTargetInfo.createDeclaration(mappings.get(0)))
            .withPaths(collectPaths(mappings))
            .withMethods(collectMethods(mappings))
            .withDeclarationFlag(true);
        return List.of(new PsiElementUrlPathInlayHint(anchor, context));
    }

    @RequiredReadAction
    private List<UrlPath> collectPaths(List<SpringRequestMapping> mappings) {
        SpringWebServerSettings settings = SpringWebServerSettings.get(ModuleUtilCore.findModuleForPsiElement(myMethod));
        List<UrlPath> paths = new ArrayList<>();
        for (SpringRequestMapping mapping : mappings) {
            paths.add(DefaultFrameworkUrlPathSpecification.INSTANCE.parsePath(mapping.getPath()));
            paths.add(DefaultFrameworkUrlPathSpecification.INSTANCE.parsePath(settings.applyTo(mapping.getPath())));
        }
        return paths;
    }

    private static Set<String> collectMethods(List<SpringRequestMapping> mappings) {
        Set<String> methods = new LinkedHashSet<>();
        for (SpringRequestMapping mapping : mappings) {
            String httpMethod = mapping.getHttpMethod();
            if (httpMethod == null) {
                return Set.of();
            }
            methods.add(httpMethod);
        }
        return methods;
    }
}
