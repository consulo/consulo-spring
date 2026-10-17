package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringWebReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new SpringClientUrlReferenceProvider());
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new SpringMappingPathReferenceProvider());
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new SpringMimeTypeReferenceProvider());
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new SpringHttpHeaderReferenceProvider());
    }

    @Override
    public Language getLanguage() {
        return JavaLanguage.INSTANCE;
    }
}
