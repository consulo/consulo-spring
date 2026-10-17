package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.parameter.PathVariableDeclarationUtil;
import consulo.endpoint.url.reference.DefaultFrameworkUrlPathSpecification;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PartiallyKnownString;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringMappingPathReferenceProvider extends PsiReferenceProvider {
    @Override
    @RequiredReadAction
    public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String value) || value.indexOf('{') < 0) {
            return PsiReference.EMPTY_ARRAY;
        }
        SpringAnnotationAttribute attribute = SpringAnnotationAttribute.find(literal);
        if (attribute == null || !attribute.is("value", "path", "url") || !SpringRequestMappingSearch.isPathAnnotation(attribute.getAnnotation())) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(literal))) {
            return PsiReference.EMPTY_ARRAY;
        }
        PartiallyKnownString pks = new PartiallyKnownString(value, literal, ElementManipulators.getValueTextRange(literal));
        return PathVariableDeclarationUtil.createPathVariableReferencesForPks(
            literal,
            pks,
            DefaultFrameworkUrlPathSpecification.INSTANCE.getParser(),
            SpringPathVariableUsagesProvider.INSTANCE
        );
    }
}
