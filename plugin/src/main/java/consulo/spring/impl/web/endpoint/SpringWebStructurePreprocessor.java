package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiCodeBlock;
import com.intellij.java.language.psi.PsiJavaFile;
import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.event.PsiTreeChangeEvent;
import consulo.language.psi.event.PsiTreeChangePreprocessor;
import consulo.project.Project;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringWebStructurePreprocessor implements PsiTreeChangePreprocessor {
    private final Project myProject;

    @Inject
    public SpringWebStructurePreprocessor(Project project) {
        myProject = project;
    }

    @Override
    public void treeChanged(PsiTreeChangeEvent event) {
        PsiFile file = event.getFile();
        if (file == null) {
            bump();
            return;
        }
        if (file instanceof PsiJavaFile) {
            if (!isInsideCodeBlock(event.getParent())) {
                bump();
            }
            return;
        }
        if (isConfigFile(file.getName())) {
            bump();
        }
    }

    private void bump() {
        SpringWebModificationTracker.getInstance(myProject).incStructure();
    }

    private static boolean isInsideCodeBlock(@Nullable PsiElement element) {
        PsiElement current = element;
        while (current != null && !(current instanceof PsiFile)) {
            if (current instanceof PsiCodeBlock) {
                return true;
            }
            if (current instanceof PsiMember) {
                return false;
            }
            current = current.getParent();
        }
        return false;
    }

    private static boolean isConfigFile(String name) {
        return name.endsWith(".properties") || name.endsWith(".yml") || name.endsWith(".yaml");
    }
}
