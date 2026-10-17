package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.JavaTokenType;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiParenthesizedExpression;
import com.intellij.java.language.psi.PsiPolyadicExpression;
import com.intellij.java.language.psi.PsiReferenceExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PartiallyKnownString;
import consulo.language.psi.util.StringEntry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class JavaPartiallyKnownStringBuilder {
    private static final int MAX_DEPTH = 10;

    private JavaPartiallyKnownStringBuilder() {
    }

    @RequiredReadAction
    public static @Nullable PartiallyKnownString build(PsiExpression expression) {
        List<StringEntry> entries = new ArrayList<>();
        collect(expression, entries, 0);
        return entries.isEmpty() ? null : new PartiallyKnownString(entries);
    }

    @RequiredReadAction
    public static PsiExpression getRootExpression(PsiExpression expression) {
        PsiExpression current = expression;
        while (true) {
            PsiElement parent = current.getParent();
            if (parent instanceof PsiParenthesizedExpression parenthesized) {
                current = parenthesized;
            }
            else if (parent instanceof PsiPolyadicExpression polyadic && polyadic.getOperationTokenType() == JavaTokenType.PLUS) {
                current = polyadic;
            }
            else {
                return current;
            }
        }
    }

    @RequiredReadAction
    private static void collect(@Nullable PsiExpression expression, List<StringEntry> entries, int depth) {
        if (expression == null) {
            return;
        }
        if (depth > MAX_DEPTH) {
            entries.add(unknown(expression));
            return;
        }
        if (expression instanceof PsiLiteralExpression literal) {
            if (literal.getValue() instanceof String value) {
                entries.add(new StringEntry.Known(value, literal, ElementManipulators.getValueTextRange(literal)));
            }
            else {
                entries.add(unknown(literal));
            }
            return;
        }
        if (expression instanceof PsiParenthesizedExpression parenthesized) {
            collect(parenthesized.getExpression(), entries, depth + 1);
            return;
        }
        if (expression instanceof PsiPolyadicExpression polyadic && polyadic.getOperationTokenType() == JavaTokenType.PLUS) {
            for (PsiExpression operand : polyadic.getOperands()) {
                collect(operand, entries, depth + 1);
            }
            return;
        }
        if (expression instanceof PsiReferenceExpression reference
            && reference.resolve() instanceof PsiField field
            && field.hasModifierProperty(PsiModifier.FINAL)) {
            PartiallyKnownString initializer = field.getInitializer() == null ? null : build(field.getInitializer());
            String value = initializer == null ? null : initializer.getValueIfKnown();
            if (value != null) {
                entries.add(new StringEntry.Known(value, reference, TextRange.from(0, reference.getTextLength())));
                return;
            }
        }
        entries.add(unknown(expression));
    }

    @RequiredReadAction
    private static StringEntry unknown(PsiExpression expression) {
        return new StringEntry.Unknown(expression, TextRange.from(0, expression.getTextLength()));
    }
}
