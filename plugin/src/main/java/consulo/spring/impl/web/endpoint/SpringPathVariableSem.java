package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.endpoint.url.parameter.PathVariablePsiElement;
import consulo.endpoint.url.parameter.PathVariableSem;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringPathVariableSem implements PathVariableSem {
    private final PsiParameter myParameter;
    private final PsiMethod myMethod;
    private final String myName;
    private final boolean myActualNameHolder;

    private SpringPathVariableSem(PsiParameter parameter, PsiMethod method, String name, boolean actualNameHolder) {
        myParameter = parameter;
        myMethod = method;
        myName = name;
        myActualNameHolder = actualNameHolder;
    }

    @RequiredReadAction
    public static @Nullable SpringPathVariableSem create(PsiParameter parameter) {
        PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.PATH_VARIABLE);
        if (annotation == null) {
            return null;
        }
        PsiMethod method = PsiTreeUtil.getParentOfType(parameter, PsiMethod.class);
        String name = SpringRequestMappingSearch.getParameterBindingName(parameter, annotation);
        if (method == null || name == null) {
            return null;
        }
        return new SpringPathVariableSem(parameter, method, name, SpringRequestMappingSearch.getExplicitBindingName(annotation) == null);
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public boolean isActualNameHolder() {
        return myActualNameHolder;
    }

    @Override
    @RequiredReadAction
    public @Nullable PathVariablePsiElement getPathVariablePsiElement() {
        if (!myParameter.isValid() || !myMethod.isValid()) {
            return null;
        }
        List<PathVariablePsiElement> elements = new ArrayList<>();
        for (PsiLiteralExpression literal : SpringRequestMappingSearch.findDeclaredPathLiterals(myMethod)) {
            if (!(literal.getValue() instanceof String value)) {
                continue;
            }
            int index = findVariable(value, myName);
            if (index < 0) {
                continue;
            }
            int offset = ElementManipulators.getValueTextRange(literal).getStartOffset() + index;
            elements.add(PathVariablePsiElement.create(
                myName,
                literal,
                TextRange.from(offset, myName.length()),
                SpringPathVariableUsagesProvider.INSTANCE
            ));
        }
        return PathVariablePsiElement.merge(elements);
    }

    private static int findVariable(String path, String name) {
        for (SpringUriTemplateVariable variable : SpringUriTemplateUtil.parse(path)) {
            if (variable.getName().equals(name)) {
                return variable.getStartOffset();
            }
        }
        return -1;
    }
}
