package consulo.spring.impl.web.inspection;

import com.intellij.java.language.psi.PsiAnnotation;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringRemoveAnnotationFix implements LocalQuickFix {
    private final String myPresentableName;

    public SpringRemoveAnnotationFix(String presentableName) {
        myPresentableName = presentableName;
    }

    @Override
    public LocalizeValue getName() {
        return SpringLocalize.webInspectionMappingConflictRemoveFix(myPresentableName);
    }

    @Override
    @RequiredWriteAction
    public void applyFix(Project project, ProblemDescriptor descriptor) {
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiAnnotation.class, false);
        if (annotation != null) {
            annotation.delete();
        }
    }
}
