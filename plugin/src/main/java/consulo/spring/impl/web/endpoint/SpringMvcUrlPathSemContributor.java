package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.url.inlay.UrlPathInlayHintsProviderSemElement;
import consulo.endpoint.url.parameter.PathVariableSem;
import consulo.endpoint.url.parameter.QueryParameterSem;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.sem.SemContributor;
import consulo.language.sem.SemRegistrar;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringMvcUrlPathSemContributor extends SemContributor {
    @Override
    @SuppressWarnings("NullAway")
    public void registerSemProviders(SemRegistrar registrar) {
        registrar.registerSemElementProvider(
            UrlPathInlayHintsProviderSemElement.INLAY_HINT_SEM_KEY,
            PlatformPatterns.psiElement(PsiAnnotation.class),
            SpringMvcUrlPathSemContributor::createInlay
        );
        registrar.registerSemElementProvider(
            PathVariableSem.PATH_VARIABLE_SEM_KEY,
            PlatformPatterns.psiElement(PsiParameter.class),
            SpringPathVariableSem::create
        );
        registrar.registerSemElementProvider(
            QueryParameterSem.QUERY_PARAMETER_SEM_KEY,
            PlatformPatterns.psiElement(PsiParameter.class),
            SpringQueryParameterSem::create
        );
    }

    @RequiredReadAction
    private static @Nullable UrlPathInlayHintsProviderSemElement createInlay(PsiAnnotation annotation) {
        SpringClientUrlPathInlaySemElement client = SpringClientUrlPathInlaySemElement.create(annotation);
        if (client != null) {
            return client;
        }
        if (SpringClientSearch.isFeignClient(PsiTreeUtil.getParentOfType(annotation, PsiClass.class))) {
            return null;
        }
        return SpringMvcUrlPathInlaySemElement.create(annotation);
    }
}
