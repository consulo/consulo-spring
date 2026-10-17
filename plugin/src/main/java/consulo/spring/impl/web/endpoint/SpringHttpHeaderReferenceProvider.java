package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.endpoint.http.HttpHeaderReference;
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
public final class SpringHttpHeaderReferenceProvider extends PsiReferenceProvider {
    @Override
    @RequiredReadAction
    public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String value) || value.isBlank()) {
            return PsiReference.EMPTY_ARRAY;
        }
        SpringAnnotationAttribute attribute = SpringAnnotationAttribute.find(literal);
        if (attribute == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        PsiAnnotation annotation = attribute.getAnnotation();
        boolean headerName = SpringWebConstants.REQUEST_HEADER.equals(annotation.getQualifiedName()) && attribute.is("value", "name");
        boolean headerCondition = attribute.is("headers") && SpringRequestMappingSearch.isPathAnnotation(annotation);
        if (!headerName && !headerCondition) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(literal))) {
            return PsiReference.EMPTY_ARRAY;
        }
        TextRange valueRange = ElementManipulators.getValueTextRange(literal);
        if (headerName) {
            return HttpHeaderReference.forElement(literal, valueRange);
        }
        int start = value.startsWith("!") ? 1 : 0;
        int end = value.indexOf('=');
        if (end < 0) {
            end = value.length();
        }
        else if (end > start && value.charAt(end - 1) == '!') {
            end--;
        }
        if (end <= start) {
            return PsiReference.EMPTY_ARRAY;
        }
        int offset = valueRange.getStartOffset();
        return HttpHeaderReference.forElement(literal, new TextRange(offset + start, offset + end));
    }
}
