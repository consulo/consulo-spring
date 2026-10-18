package consulo.spring.impl.web.inspection;

import com.intellij.java.analysis.impl.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.impl.web.endpoint.SpringClientSearch;
import consulo.spring.impl.web.endpoint.SpringEndpointProviderBase;
import consulo.spring.impl.web.endpoint.SpringMappingIndex;
import consulo.spring.impl.web.endpoint.SpringRequestMapping;
import consulo.spring.impl.web.endpoint.SpringRequestMappingSearch;
import consulo.spring.localize.SpringLocalize;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringDuplicateMappingInspection extends BaseJavaLocalInspectionTool<Object> {
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
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null || containingClass.isInterface() || SpringClientSearch.isClient(containingClass)) {
            return;
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(method);
        if (module == null || !SpringEndpointProviderBase.hasSpring(module)) {
            return;
        }
        List<SpringRequestMapping> mappings = SpringRequestMappingSearch.findMappings(method);
        if (mappings.isEmpty()) {
            return;
        }

        Map<String, List<SpringRequestMapping>> index = SpringMappingIndex.get(module);
        Set<String> reported = new HashSet<>();
        for (SpringRequestMapping mapping : mappings) {
            List<SpringRequestMapping> sameKey = index.get(SpringMappingIndex.getKey(mapping));
            if (sameKey == null) {
                continue;
            }
            Set<String> others = new LinkedHashSet<>();
            for (SpringRequestMapping other : sameKey) {
                PsiMethod otherMethod = other.getMethod();
                if (otherMethod != method && otherMethod.isValid()) {
                    others.add(getLocation(otherMethod));
                }
            }
            String presentation = SpringMappingIndex.getPresentation(mapping);
            if (others.isEmpty() || !reported.add(presentation)) {
                continue;
            }
            PsiAnnotation annotation = mapping.getAnnotation();
            if (annotation == null) {
                continue;
            }
            PsiJavaCodeReferenceElement reference = annotation.getNameReferenceElement();
            holder.newProblem(SpringLocalize.webInspectionDuplicateMapping(presentation, String.join(", ", others)))
                .range(reference != null ? reference : annotation)
                .create();
        }
    }

    @RequiredReadAction
    private static String getLocation(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        return (containingClass == null ? "" : containingClass.getName() + ".") + method.getName() + "()";
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.webInspectionGroupName();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.webInspectionDuplicateMappingDisplayName();
    }

    @Override
    public String getShortName() {
        return "SpringDuplicateMappingInspection";
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
