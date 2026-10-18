package consulo.spring.impl.web.endpoint;

import java.util.Locale;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringPluralUtil {
    private static final Map<String, String> IRREGULAR = Map.of(
        "person", "people",
        "man", "men",
        "woman", "women",
        "child", "children",
        "mouse", "mice",
        "goose", "geese",
        "foot", "feet",
        "tooth", "teeth",
        "ox", "oxen"
    );

    private SpringPluralUtil() {
    }

    public static String uncapitalizedPlural(String name) {
        if (name.isEmpty()) {
            return name;
        }
        String word = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        String irregular = IRREGULAR.get(word.toLowerCase(Locale.ROOT));
        if (irregular != null) {
            return irregular;
        }
        String lower = word.toLowerCase(Locale.ROOT);
        if (lower.endsWith("s") || lower.endsWith("x") || lower.endsWith("z") || lower.endsWith("ch") || lower.endsWith("sh")) {
            return word + "es";
        }
        if (lower.endsWith("y") && lower.length() > 1 && "aeiou".indexOf(lower.charAt(lower.length() - 2)) < 0) {
            return word.substring(0, word.length() - 1) + "ies";
        }
        return word + "s";
    }
}
