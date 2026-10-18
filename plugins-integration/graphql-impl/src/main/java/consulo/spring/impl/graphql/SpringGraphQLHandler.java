package consulo.spring.impl.graphql;

import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringGraphQLHandler {
    private final PsiMethod myMethod;
    private final String myTypeName;
    private final String myFieldName;

    public SpringGraphQLHandler(PsiMethod method, String typeName, String fieldName) {
        myMethod = method;
        myTypeName = typeName;
        myFieldName = fieldName;
    }

    public PsiMethod getMethod() {
        return myMethod;
    }

    public String getTypeName() {
        return myTypeName;
    }

    public String getFieldName() {
        return myFieldName;
    }

    public String getPresentableName() {
        return myTypeName + "." + myFieldName;
    }

    @RequiredReadAction
    public boolean isValid() {
        return myMethod.isValid();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpringGraphQLHandler that)) {
            return false;
        }
        return myMethod.equals(that.myMethod) && myTypeName.equals(that.myTypeName) && myFieldName.equals(that.myFieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myMethod, myTypeName, myFieldName);
    }

    @Override
    public String toString() {
        return getPresentableName();
    }
}
