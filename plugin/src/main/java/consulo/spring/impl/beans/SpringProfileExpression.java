package consulo.spring.impl.beans;

import java.util.Collection;
import java.util.Set;

/**
 * Spring profile expressions as {@code @Profile} and {@code <beans profile>} take them: a profile name, {@code !},
 * {@code &}, {@code |} and parentheses. A malformed expression counts as matching, so a typo never hides a bean.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringProfileExpression {
    private final String myText;
    private int myIndex;

    private SpringProfileExpression(String text) {
        myText = text;
    }

    /**
     * Spring activates a definition when any of its expressions matches.
     */
    public static boolean matchesAny(Collection<String> expressions, Set<String> activeProfiles) {
        if (expressions.isEmpty()) {
            return true;
        }
        for (String expression : expressions) {
            if (matches(expression, activeProfiles)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String expression, Set<String> activeProfiles) {
        SpringProfileExpression parser = new SpringProfileExpression(expression);
        try {
            boolean result = parser.parseOr(activeProfiles);
            parser.skipSpaces();
            return parser.myIndex < expression.length() || result;
        }
        catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Adds the profile names an expression mentions, without the operators.
     */
    public static void collectNames(String expression, Collection<String> names) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (isNameChar(c)) {
                name.append(c);
            }
            else {
                flush(name, names);
            }
        }
        flush(name, names);
    }

    private static void flush(StringBuilder name, Collection<String> names) {
        if (!name.isEmpty()) {
            String value = name.toString();
            if (!names.contains(value)) {
                names.add(value);
            }
            name.setLength(0);
        }
    }

    private boolean parseOr(Set<String> active) {
        boolean result = parseAnd(active);
        while (consume('|')) {
            boolean right = parseAnd(active);
            result = result || right;
        }
        return result;
    }

    private boolean parseAnd(Set<String> active) {
        boolean result = parseUnary(active);
        while (consume('&')) {
            boolean right = parseUnary(active);
            result = result && right;
        }
        return result;
    }

    private boolean parseUnary(Set<String> active) {
        if (consume('!')) {
            return !parseUnary(active);
        }
        if (consume('(')) {
            boolean result = parseOr(active);
            if (!consume(')')) {
                throw new IllegalArgumentException(myText);
            }
            return result;
        }
        skipSpaces();
        int start = myIndex;
        while (myIndex < myText.length() && isNameChar(myText.charAt(myIndex))) {
            myIndex++;
        }
        if (start == myIndex) {
            throw new IllegalArgumentException(myText);
        }
        return active.contains(myText.substring(start, myIndex));
    }

    private boolean consume(char c) {
        skipSpaces();
        if (myIndex < myText.length() && myText.charAt(myIndex) == c) {
            myIndex++;
            return true;
        }
        return false;
    }

    private void skipSpaces() {
        while (myIndex < myText.length() && Character.isWhitespace(myText.charAt(myIndex))) {
            myIndex++;
        }
    }

    private static boolean isNameChar(char c) {
        return !Character.isWhitespace(c) && c != '!' && c != '&' && c != '|' && c != '(' && c != ')' && c != ',';
    }
}
