package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiParameter;
import consulo.endpoint.url.UrlQueryParameter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringQueryParameter implements UrlQueryParameter {
    private final String myName;
    private final boolean myRepeatable;
    private final SmartPsiElementPointer<PsiParameter> myPointer;

    public SpringQueryParameter(String name, boolean repeatable, PsiParameter parameter) {
        myName = name;
        myRepeatable = repeatable;
        myPointer = SmartPointerManager.createPointer(parameter);
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public @Nullable String getDescription() {
        return null;
    }

    @Override
    public boolean getRepeatable() {
        return myRepeatable;
    }

    @Override
    public @Nullable PsiElement resolveToPsiElement() {
        return myPointer.getElement();
    }
}
