package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringClientEndpoint {
    private final SpringRequestMapping myMapping;
    private final SpringClientBaseUrl myBaseUrl;

    public SpringClientEndpoint(SpringRequestMapping mapping, SpringClientBaseUrl baseUrl) {
        myMapping = mapping;
        myBaseUrl = baseUrl;
    }

    public SpringRequestMapping getMapping() {
        return myMapping;
    }

    public SpringClientBaseUrl getBaseUrl() {
        return myBaseUrl;
    }

    @RequiredReadAction
    public boolean isValid() {
        return myMapping.isValid();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpringClientEndpoint that)) {
            return false;
        }
        return myMapping.equals(that.myMapping) && myBaseUrl.equals(that.myBaseUrl);
    }

    @Override
    public int hashCode() {
        return 31 * myMapping.hashCode() + myBaseUrl.hashCode();
    }

    @Override
    public String toString() {
        return myMapping + " @ " + myBaseUrl;
    }
}
