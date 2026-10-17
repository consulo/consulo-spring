package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.endpoint.mime.MimeTypeReference;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringMimeTypeReferenceProvider extends PsiReferenceProvider {
    @Override
    @RequiredReadAction
    public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String value) || value.isBlank()) {
            return PsiReference.EMPTY_ARRAY;
        }
        SpringAnnotationAttribute attribute = SpringAnnotationAttribute.find(literal);
        if (attribute == null || !isMimeAttribute(attribute)) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(literal))) {
            return PsiReference.EMPTY_ARRAY;
        }
        TextRange range = ElementManipulators.getValueTextRange(literal);
        if (value.startsWith("!")) {
            range = new TextRange(range.getStartOffset() + 1, range.getEndOffset());
        }
        return MimeTypeReference.forElement(literal, range);
    }

    @RequiredReadAction
    private static boolean isMimeAttribute(SpringAnnotationAttribute attribute) {
        PsiAnnotation annotation = attribute.getAnnotation();
        if (attribute.is("consumes", "produces")) {
            return SpringRequestMappingSearch.isMappingAnnotation(annotation);
        }
        if (attribute.is("contentType", "accept")) {
            return SpringClientSearch.isExchangeAnnotation(annotation);
        }
        return false;
    }
}
