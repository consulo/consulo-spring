package consulo.spring.impl.web.inspection;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.impl.web.endpoint.SpringWebConstants;
import consulo.spring.localize.SpringLocalize;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringAddPathVariableFix implements LocalQuickFix {
    private final String myName;

    public SpringAddPathVariableFix(String name) {
        myName = name;
    }

    @Override
    public LocalizeValue getName() {
        return SpringLocalize.webInspectionPathVariableAddFix(myName);
    }

    @Override
    @RequiredWriteAction
    public void applyFix(Project project, ProblemDescriptor descriptor) {
        PsiMethod method = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiMethod.class, false);
        if (method == null) {
            return;
        }
        PsiParameter parameter = JavaPsiFacade.getElementFactory(project)
            .createParameterFromText("@" + SpringWebConstants.PATH_VARIABLE + " String " + myName, method);
        PsiElement added = method.getParameterList().add(parameter);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(added);
    }
}
