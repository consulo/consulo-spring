package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiDocCommentOwner;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringRequestMapping {
    private static final String DEPRECATED = "java.lang.Deprecated";
    private static final String SHORT_DEPRECATED = "Deprecated";

    private final PsiMethod myMethod;
    private final @Nullable PsiAnnotation myAnnotation;
    private final PsiElement myDeclaration;
    private final String myPath;
    private final @Nullable String myHttpMethod;

    public SpringRequestMapping(PsiMethod method, PsiAnnotation annotation, String path, @Nullable String httpMethod) {
        this(method, annotation, annotation, path, httpMethod);
    }

    private SpringRequestMapping(PsiMethod method, @Nullable PsiAnnotation annotation, PsiElement declaration, String path, @Nullable String httpMethod) {
        myMethod = method;
        myAnnotation = annotation;
        myDeclaration = declaration;
        myPath = path;
        myHttpMethod = httpMethod;
    }

    public static SpringRequestMapping route(PsiMethod handler, PsiElement declaration, String path, @Nullable String httpMethod) {
        return new SpringRequestMapping(handler, null, declaration, path, httpMethod);
    }

    public PsiMethod getMethod() {
        return myMethod;
    }

    public @Nullable PsiAnnotation getAnnotation() {
        return myAnnotation;
    }

    public PsiElement getDeclaration() {
        return myDeclaration;
    }

    public boolean isRoute() {
        return myAnnotation == null;
    }

    public String getPath() {
        return myPath;
    }

    public @Nullable String getHttpMethod() {
        return myHttpMethod;
    }

    @RequiredReadAction
    public boolean isValid() {
        return myMethod.isValid() && myDeclaration.isValid();
    }

    @RequiredReadAction
    public boolean isDeprecated() {
        if (isDeprecated(myMethod)) {
            return true;
        }
        PsiClass containingClass = myMethod.getContainingClass();
        return containingClass != null && isDeprecated(containingClass);
    }

    @RequiredReadAction
    private static boolean isDeprecated(PsiModifierListOwner owner) {
        if (owner instanceof PsiDocCommentOwner docOwner && docOwner.isDeprecated()) {
            return true;
        }
        PsiModifierList modifierList = owner.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            PsiJavaCodeReferenceElement reference = annotation.getNameReferenceElement();
            if (reference != null && DEPRECATED.equals(reference.getQualifiedName())) {
                return true;
            }
            if (reference != null && reference.resolve() == null && SHORT_DEPRECATED.equals(reference.getReferenceName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpringRequestMapping that)) {
            return false;
        }
        return myMethod.equals(that.myMethod)
            && myDeclaration.equals(that.myDeclaration)
            && myPath.equals(that.myPath)
            && Objects.equals(myHttpMethod, that.myHttpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myMethod, myPath, myHttpMethod);
    }

    @Override
    public String toString() {
        return (myHttpMethod == null ? "*" : myHttpMethod) + " " + myPath;
    }
}
