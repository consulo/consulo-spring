package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.parameter.PathVariablePomTarget;
import consulo.endpoint.url.parameter.PathVariableUsagesProvider;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringPathVariableUsagesProvider implements PathVariableUsagesProvider {
    public static final SpringPathVariableUsagesProvider INSTANCE = new SpringPathVariableUsagesProvider();

    private SpringPathVariableUsagesProvider() {
    }

    @Override
    @RequiredReadAction
    public Iterable<LookupElement> getCompletionVariantsForDeclaration(PsiElement context) {
        String text = context instanceof PsiLiteralExpression literal && literal.getValue() instanceof String value ? value : "";
        Set<String> names = new LinkedHashSet<>();
        for (PsiMethod method : findHandlerMethods(context)) {
            for (PsiParameter parameter : method.getParameterList().getParameters()) {
                PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.PATH_VARIABLE);
                String name = annotation == null ? null : SpringRequestMappingSearch.getParameterBindingName(parameter, annotation);
                if (name != null && !text.contains("{" + name + "}") && !text.contains("{" + name + ":")) {
                    names.add(name);
                }
            }
        }
        List<LookupElement> result = new ArrayList<>(names.size());
        for (String name : names) {
            result.add(LookupElementBuilder.create(name));
        }
        return result;
    }

    @Override
    @RequiredReadAction
    public Iterable<PsiElement> findSemDefiningElements(PathVariablePomTarget pomTarget) {
        String name = pomTarget.getName();
        List<PsiElement> result = new ArrayList<>();
        for (PsiMethod method : findHandlerMethods(pomTarget.getScope())) {
            for (PsiParameter parameter : method.getParameterList().getParameters()) {
                PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.PATH_VARIABLE);
                if (annotation != null && name.equals(SpringRequestMappingSearch.getParameterBindingName(parameter, annotation))) {
                    result.add(parameter);
                }
            }
        }
        return result;
    }

    @RequiredReadAction
    private static List<PsiMethod> findHandlerMethods(PsiElement context) {
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(context, PsiAnnotation.class);
        if (annotation == null || !(annotation.getParent() instanceof PsiModifierList modifierList)) {
            return List.of();
        }
        PsiElement owner = modifierList.getParent();
        if (owner instanceof PsiMethod method) {
            return List.of(method);
        }
        if (owner instanceof PsiClass psiClass) {
            List<PsiMethod> result = new ArrayList<>();
            for (PsiMethod method : psiClass.getMethods()) {
                if (!SpringRequestMappingSearch.findMappings(method).isEmpty() || SpringClientSearch.hasExchangeAnnotation(method)) {
                    result.add(method);
                }
            }
            return result;
        }
        return List.of();
    }
}
