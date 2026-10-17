package consulo.spring.impl.web.endpoint;

import com.intellij.lang.properties.IProperty;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.spring.boot.SpringConfigPropertyContributor;
import consulo.spring.impl.boot.properties.SpringConfigurationPropertySearch;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringConfigValues {
    private SpringConfigValues() {
    }

    @RequiredReadAction
    public static @Nullable String readValue(Module module, String key) {
        for (PsiElement element : SpringConfigurationPropertySearch.getInstance(module.getProject()).resolvePropertyKey(key, module)) {
            String value = getValue(module, element);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    @RequiredReadAction
    public static String resolvePlaceholders(@Nullable Module module, String text) {
        if (!text.contains("${")) {
            return text;
        }
        return resolvePlaceholders(module, text, new HashSet<>());
    }

    @RequiredReadAction
    private static String resolvePlaceholders(@Nullable Module module, String text, Set<String> visiting) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf("${", index);
            if (start < 0) {
                result.append(text, index, text.length());
                break;
            }
            int end = findPlaceholderEnd(text, start);
            if (end < 0) {
                result.append(text, index, text.length());
                break;
            }
            result.append(text, index, start);

            String expression = text.substring(start + 2, end);
            int defaultSeparator = expression.indexOf(':');
            String key = defaultSeparator < 0 ? expression : expression.substring(0, defaultSeparator);
            String defaultValue = defaultSeparator < 0 ? null : expression.substring(defaultSeparator + 1);

            String value = null;
            if (visiting.add(key)) {
                value = module == null ? null : readValue(module, key);
                if (value != null) {
                    value = resolvePlaceholders(module, value, visiting);
                }
                visiting.remove(key);
            }
            if (value == null && defaultValue != null) {
                value = resolvePlaceholders(module, defaultValue, visiting);
            }
            result.append(value != null ? value : text.substring(start, end + 1));
            index = end + 1;
        }
        return result.toString();
    }

    private static int findPlaceholderEnd(String text, int start) {
        int depth = 0;
        for (int i = start + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') {
                depth++;
            }
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    @RequiredReadAction
    private static @Nullable String getValue(Module module, PsiElement element) {
        if (element instanceof IProperty property) {
            return property.getValue();
        }
        return module.getProject().getApplication()
            .getExtensionPoint(SpringConfigPropertyContributor.class)
            .computeSafeIfAny(contributor -> contributor.getValue(element));
    }
}
