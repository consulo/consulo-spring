package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.PsiAnnotation;
import consulo.annotation.component.ExtensionImpl;
import consulo.endpoint.url.inlay.UrlPathInlayLanguagesProvider;
import consulo.language.Language;
import consulo.language.psi.PsiElement;

import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringMvcUrlPathInlayLanguagesProvider implements UrlPathInlayLanguagesProvider {
    @Override
    public Collection<Language> getLanguages() {
        return List.of(JavaLanguage.INSTANCE);
    }

    @Override
    public List<PsiElement> getPotentialElementsWithHintsProviders(PsiElement element) {
        return element instanceof PsiAnnotation ? List.of(element) : List.of();
    }
}
