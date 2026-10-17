package consulo.spring.impl.web.endpoint;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringUriTemplateUtil {
    private static final String PLACEHOLDER_PREFIX = "${";

    private SpringUriTemplateUtil() {
    }

    public static List<SpringUriTemplateVariable> parse(String text) {
        List<SpringUriTemplateVariable> result = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf('{', index);
            if (start < 0) {
                break;
            }
            int end = findClosingBrace(text, start);
            if (end < 0) {
                break;
            }
            if (start == 0 || text.charAt(start - 1) != '$') {
                int nameStart = start + 1;
                if (nameStart < end && text.charAt(nameStart) == '*') {
                    nameStart++;
                }
                int nameEnd = nameStart;
                while (nameEnd < end && text.charAt(nameEnd) != ':') {
                    nameEnd++;
                }
                if (nameEnd > nameStart) {
                    result.add(new SpringUriTemplateVariable(text.substring(nameStart, nameEnd), nameStart, nameEnd));
                }
            }
            index = end + 1;
        }
        return result;
    }

    public static Set<String> names(String text) {
        Set<String> result = new LinkedHashSet<>();
        for (SpringUriTemplateVariable variable : parse(text)) {
            result.add(variable.getName());
        }
        return result;
    }

    public static boolean hasPlaceholder(String text) {
        return text.contains(PLACEHOLDER_PREFIX);
    }

    public static String normalize(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        int index = 0;
        while (index < text.length()) {
            int start = text.indexOf('{', index);
            if (start < 0) {
                break;
            }
            int end = findClosingBrace(text, start);
            if (end < 0) {
                break;
            }
            if (start > 0 && text.charAt(start - 1) == '$') {
                builder.append(text, index, end + 1);
            }
            else {
                builder.append(text, index, start).append("{}");
            }
            index = end + 1;
        }
        builder.append(text, index, text.length());
        return builder.toString();
    }

    private static int findClosingBrace(String text, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < text.length(); i++) {
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
}
