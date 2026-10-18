package consulo.spring.impl.web.endpoint;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringRoutePredicate {
    public static final SpringRoutePredicate ANY = new SpringRoutePredicate(null, "");

    private final @Nullable String myHttpMethod;
    private final String myPath;

    public SpringRoutePredicate(@Nullable String httpMethod, String path) {
        myHttpMethod = httpMethod;
        myPath = path;
    }

    public @Nullable String getHttpMethod() {
        return myHttpMethod;
    }

    public String getPath() {
        return myPath;
    }

    public @Nullable SpringRoutePredicate and(SpringRoutePredicate other) {
        if (myHttpMethod != null && other.myHttpMethod != null && !myHttpMethod.equals(other.myHttpMethod)) {
            return null;
        }
        return new SpringRoutePredicate(myHttpMethod != null ? myHttpMethod : other.myHttpMethod, myPath + other.myPath);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpringRoutePredicate that)) {
            return false;
        }
        return Objects.equals(myHttpMethod, that.myHttpMethod) && myPath.equals(that.myPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myHttpMethod, myPath);
    }

    @Override
    public String toString() {
        return (myHttpMethod == null ? "*" : myHttpMethod) + " " + myPath;
    }
}
