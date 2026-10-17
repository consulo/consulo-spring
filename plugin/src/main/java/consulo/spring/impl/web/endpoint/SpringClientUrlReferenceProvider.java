package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.UrlConstants;
import consulo.endpoint.url.reference.UrlExtractorUtil;
import consulo.endpoint.url.reference.UrlPathContext;
import consulo.endpoint.url.reference.UrlPathReferenceInjector;
import consulo.endpoint.url.reference.UrlPksParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PartiallyKnownString;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;

import java.util.Locale;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringClientUrlReferenceProvider extends PsiReferenceProvider {
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "0.0.0.0", "[::1]");

    private final UrlPathReferenceInjector<PsiExpression> myInjector =
        UrlPathReferenceInjector.<PsiExpression>forPartialStringFrom(createParser(), JavaPartiallyKnownStringBuilder::build)
            .withDefaultRootContextProviderFactory(SpringClientUrlReferenceProvider::createRootContext);

    @Override
    @RequiredReadAction
    public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String)) {
            return PsiReference.EMPTY_ARRAY;
        }
        PsiExpression root = JavaPartiallyKnownStringBuilder.getRootExpression(literal);
        if (SpringClientCall.findCallCandidate(root) == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(literal))) {
            return PsiReference.EMPTY_ARRAY;
        }
        if (SpringClientCall.find(root) == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        PartiallyKnownString pks = JavaPartiallyKnownStringBuilder.build(root);
        if (pks == null || isExternal(pks.getConcatenationOfKnown())) {
            return PsiReference.EMPTY_ARRAY;
        }
        return myInjector.buildAbsoluteOrRelativeReferences(root, literal);
    }

    private static UrlPksParser createParser() {
        UrlPksParser parser = new UrlPksParser(UrlExtractorUtil::springLikePropertySplitEscaper);
        parser.setParseQueryParameters(true);
        return parser;
    }

    @RequiredReadAction
    private static UrlPathContext createRootContext(PsiExpression root) {
        SpringClientCall call = SpringClientCall.find(root);
        return UrlPathContext.supportingSchemes(UrlConstants.HTTP_SCHEMES, call == null ? null : call.getHttpMethod());
    }

    private static boolean isExternal(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        int schemeEnd = lower.indexOf("://");
        if (schemeEnd < 0) {
            return false;
        }
        int authorityStart = schemeEnd + 3;
        int authorityEnd = lower.indexOf('/', authorityStart);
        String authority = authorityEnd < 0 ? lower.substring(authorityStart) : lower.substring(authorityStart, authorityEnd);
        String host = authority.startsWith("[") ? authority.substring(0, authority.indexOf(']') + 1) : authority.split(":", 2)[0];
        return !LOCAL_HOSTS.contains(host);
    }
}
