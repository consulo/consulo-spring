package consulo.spring.impl.web.endpoint;

import consulo.endpoint.url.UrlConstants;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringClientBaseUrl {
    public static final SpringClientBaseUrl RELATIVE = new SpringClientBaseUrl(null, null, "");

    private static final String SCHEME_SEPARATOR = "://";

    private final @Nullable String myScheme;
    private final @Nullable String myAuthority;
    private final String myPath;

    private SpringClientBaseUrl(@Nullable String scheme, @Nullable String authority, String path) {
        myScheme = scheme;
        myAuthority = authority;
        myPath = path;
    }

    public static SpringClientBaseUrl parse(@Nullable String url, boolean schemelessIsAuthority) {
        if (url == null || url.isBlank() || url.contains("${")) {
            return RELATIVE;
        }
        String text = url.trim();
        int schemeEnd = text.indexOf(SCHEME_SEPARATOR);
        String scheme;
        String rest;
        if (schemeEnd > 0) {
            scheme = text.substring(0, schemeEnd + SCHEME_SEPARATOR.length()).toLowerCase(Locale.ROOT);
            rest = text.substring(schemeEnd + SCHEME_SEPARATOR.length());
        }
        else if (schemelessIsAuthority && !text.startsWith("/")) {
            scheme = UrlConstants.HTTP_SCHEME;
            rest = text;
        }
        else {
            return new SpringClientBaseUrl(null, null, stripQuery(text));
        }

        int slash = rest.indexOf('/');
        String authority = slash < 0 ? stripQuery(rest) : rest.substring(0, slash);
        String path = slash < 0 ? "" : stripQuery(rest.substring(slash));
        return new SpringClientBaseUrl(scheme, authority.isEmpty() ? null : authority, path);
    }

    private static String stripQuery(String text) {
        int query = text.indexOf('?');
        return query < 0 ? text : text.substring(0, query);
    }

    public @Nullable String getScheme() {
        return myScheme;
    }

    public @Nullable String getAuthority() {
        return myAuthority;
    }

    public String getPath() {
        return myPath;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpringClientBaseUrl that)) {
            return false;
        }
        return Objects.equals(myScheme, that.myScheme) && Objects.equals(myAuthority, that.myAuthority) && myPath.equals(that.myPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myScheme, myAuthority, myPath);
    }

    @Override
    public String toString() {
        return (myScheme == null ? "" : myScheme) + (myAuthority == null ? "" : myAuthority) + myPath;
    }
}
