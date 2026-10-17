package consulo.spring.impl.web.inspection;

import com.intellij.java.analysis.impl.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiIdentifier;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiNameHelper;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemBuilder;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.spring.impl.web.endpoint.SpringClientEndpoint;
import consulo.spring.impl.web.endpoint.SpringClientSearch;
import consulo.spring.impl.web.endpoint.SpringEndpointProviderBase;
import consulo.spring.impl.web.endpoint.SpringRequestMapping;
import consulo.spring.impl.web.endpoint.SpringRequestMappingSearch;
import consulo.spring.impl.web.endpoint.SpringUriTemplateUtil;
import consulo.spring.impl.web.endpoint.SpringUriTemplateVariable;
import consulo.spring.impl.web.endpoint.SpringWebConstants;
import consulo.spring.localize.SpringLocalize;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringPathVariableInspection extends BaseJavaLocalInspectionTool<Object> {
    @Override
    public PsiElementVisitor buildVisitorImpl(ProblemsHolder holder, boolean isOnTheFly, LocalInspectionToolSession session, Object state) {
        return new JavaElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitMethod(PsiMethod method) {
                checkMethod(method, holder);
            }
        };
    }

    @RequiredReadAction
    private static void checkMethod(PsiMethod method, ProblemsHolder holder) {
        if (!SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(method))) {
            return;
        }
        List<String> paths = findPaths(method);
        if (paths.isEmpty()) {
            return;
        }

        Map<String, PsiParameter> bound = new LinkedHashMap<>();
        boolean bindsAll = false;
        boolean hasModelAttribute = false;
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            if (SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.MODEL_ATTRIBUTE) != null) {
                hasModelAttribute = true;
            }
            PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.PATH_VARIABLE);
            if (annotation == null) {
                continue;
            }
            String name = SpringRequestMappingSearch.getParameterBindingName(parameter, annotation);
            if (name == null) {
                bindsAll = true;
            }
            else {
                bound.putIfAbsent(name, parameter);
            }
        }

        Set<String> declared = new LinkedHashSet<>();
        boolean fullyKnown = true;
        for (String path : paths) {
            declared.addAll(SpringUriTemplateUtil.names(path));
            fullyKnown &= !SpringUriTemplateUtil.hasPlaceholder(path);
        }

        if (!bindsAll && !hasModelAttribute) {
            for (String name : declared) {
                if (!bound.containsKey(name)) {
                    reportMissing(method, name, holder);
                }
            }
        }

        if (fullyKnown) {
            for (Map.Entry<String, PsiParameter> entry : bound.entrySet()) {
                if (!declared.contains(entry.getKey())) {
                    PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(entry.getValue(), SpringWebConstants.PATH_VARIABLE);
                    if (annotation != null) {
                        holder.newProblem(SpringLocalize.webInspectionPathVariableUnmatched(entry.getKey()))
                            .range(annotation)
                            .create();
                    }
                }
            }
        }
    }

    @RequiredReadAction
    private static void reportMissing(PsiMethod method, String name, ProblemsHolder holder) {
        ProblemBuilder builder = holder.newProblem(SpringLocalize.webInspectionPathVariableMissing(name));
        PsiLiteralExpression literal = null;
        TextRange range = null;
        for (PsiAnnotation annotation : method.getModifierList().getAnnotations()) {
            if (!SpringRequestMappingSearch.isPathAnnotation(annotation)) {
                continue;
            }
            for (PsiLiteralExpression candidate : SpringRequestMappingSearch.findPathLiterals(annotation)) {
                if (!(candidate.getValue() instanceof String value)) {
                    continue;
                }
                for (SpringUriTemplateVariable variable : SpringUriTemplateUtil.parse(value)) {
                    if (variable.getName().equals(name)) {
                        int offset = ElementManipulators.getValueTextRange(candidate).getStartOffset();
                        literal = candidate;
                        range = new TextRange(offset + variable.getStartOffset(), offset + variable.getEndOffset());
                        break;
                    }
                }
                if (literal != null) {
                    break;
                }
            }
            if (literal != null) {
                break;
            }
        }

        if (literal != null) {
            builder.range(literal, range);
        }
        else {
            PsiIdentifier identifier = method.getNameIdentifier();
            if (identifier == null) {
                return;
            }
            builder.range(identifier);
        }
        if (PsiNameHelper.getInstance(method.getProject()).isIdentifier(name)) {
            builder.withFix(new SpringAddPathVariableFix(name));
        }
        builder.create();
    }

    @RequiredReadAction
    private static List<String> findPaths(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        List<String> result = new ArrayList<>();
        if (SpringClientSearch.isClient(containingClass)) {
            for (SpringClientEndpoint endpoint : SpringClientSearch.findEndpoints(method)) {
                result.add(endpoint.getMapping().getPath());
            }
        }
        else {
            for (SpringRequestMapping mapping : SpringRequestMappingSearch.findMappings(method)) {
                result.add(mapping.getPath());
            }
        }
        return result;
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.webInspectionGroupName();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.webInspectionPathVariableDisplayName();
    }

    @Override
    public String getShortName() {
        return "SpringPathVariableInspection";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
