package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationParameterList;
import com.intellij.java.language.psi.PsiArrayInitializerMemberValue;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiNameValuePair;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringAnnotationAttribute {
    private static final String DEFAULT_ATTRIBUTE = "value";

    private final PsiAnnotation myAnnotation;
    private final String myName;

    private SpringAnnotationAttribute(PsiAnnotation annotation, String name) {
        myAnnotation = annotation;
        myName = name;
    }

    @RequiredReadAction
    public static @Nullable SpringAnnotationAttribute find(PsiLiteralExpression literal) {
        PsiElement parent = literal.getParent();
        if (parent instanceof PsiArrayInitializerMemberValue) {
            parent = parent.getParent();
        }
        if (!(parent instanceof PsiNameValuePair pair)) {
            return null;
        }
        if (pair.getParent() instanceof PsiAnnotationParameterList parameterList && parameterList.getParent() instanceof PsiAnnotation annotation) {
            String name = pair.getName();
            return new SpringAnnotationAttribute(annotation, name == null ? DEFAULT_ATTRIBUTE : name);
        }
        return null;
    }

    public PsiAnnotation getAnnotation() {
        return myAnnotation;
    }

    public String getName() {
        return myName;
    }

    public boolean is(String... names) {
        for (String name : names) {
            if (myName.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
