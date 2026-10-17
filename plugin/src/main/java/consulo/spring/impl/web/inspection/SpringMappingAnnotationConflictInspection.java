package consulo.spring.impl.web.inspection;

import com.intellij.java.analysis.impl.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.spring.impl.web.endpoint.SpringEndpointProviderBase;
import consulo.spring.impl.web.endpoint.SpringRequestMappingSearch;
import consulo.spring.localize.SpringLocalize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringMappingAnnotationConflictInspection extends BaseJavaLocalInspectionTool<Object> {
    @Override
    public PsiElementVisitor buildVisitorImpl(ProblemsHolder holder, boolean isOnTheFly, LocalInspectionToolSession session, Object state) {
        return new JavaElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitMethod(PsiMethod method) {
                check(method, holder);
            }

            @Override
            @RequiredReadAction
            public void visitClass(PsiClass aClass) {
                check(aClass, holder);
            }
        };
    }

    @RequiredReadAction
    private static void check(PsiModifierListOwner owner, ProblemsHolder holder) {
        PsiModifierList modifierList = owner.getModifierList();
        if (modifierList == null) {
            return;
        }
        List<PsiAnnotation> mappings = new ArrayList<>();
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            if (SpringRequestMappingSearch.isPathAnnotation(annotation)) {
                mappings.add(annotation);
            }
        }
        if (mappings.size() < 2 || !SpringEndpointProviderBase.hasSpring(ModuleUtilCore.findModuleForPsiElement(owner))) {
            return;
        }
        String first = getPresentableName(mappings.get(0));
        for (PsiAnnotation annotation : mappings.subList(1, mappings.size())) {
            PsiJavaCodeReferenceElement reference = annotation.getNameReferenceElement();
            holder.newProblem(SpringLocalize.webInspectionMappingConflict(first))
                .range(reference != null ? reference : annotation)
                .withFix(new SpringRemoveAnnotationFix(getPresentableName(annotation)))
                .create();
        }
    }

    @RequiredReadAction
    private static String getPresentableName(PsiAnnotation annotation) {
        PsiJavaCodeReferenceElement reference = annotation.getNameReferenceElement();
        String name = reference == null ? null : reference.getReferenceName();
        return "@" + (name == null ? annotation.getQualifiedName() : name);
    }

    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.webInspectionGroupName();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.webInspectionMappingConflictDisplayName();
    }

    @Override
    public String getShortName() {
        return "SpringMappingAnnotationConflictInspection";
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
