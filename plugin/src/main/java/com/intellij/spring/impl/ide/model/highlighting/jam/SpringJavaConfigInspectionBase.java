package com.intellij.spring.impl.ide.model.highlighting.jam;

import com.intellij.jam.JamService;
import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.java.analysis.impl.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaSpringConfigurationElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.sem.SemKey;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.impl.boot.jam.SpringBootConfigurationElement;
import consulo.spring.impl.module.extension.SpringModuleExtension;

import consulo.spring.localize.SpringLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class SpringJavaConfigInspectionBase extends BaseJavaLocalInspectionTool<Object> {
    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.modelInspectionGroupName();
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public PsiElementVisitor buildVisitorImpl(
        @Nonnull ProblemsHolder holder,
        boolean isOnTheFly,
        LocalInspectionToolSession session,
        Object o
    ) {
        PsiFile file = holder.getFile();
        if (!JamCommonUtil.isPlainJavaFile(file)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        final consulo.module.Module module = file.getModule();
        if (module == null || SpringModuleExtension.getInstance(module) == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new JavaElementVisitor() {
            @Override
            public void visitClass(@Nonnull PsiClass aClass) {
                checkClass(aClass, holder, module);
            }
        };
    }

    protected void checkClass(PsiClass aClass, ProblemsHolder holder, @Nonnull Module module) {
        SpringJamElement configuration = getJavaConfiguration(aClass, module);

        if (configuration != null) {
            checkJavaConfiguration(configuration, module, holder);
        }
    }

    @Nullable
    protected SpringJamElement getJavaConfiguration(PsiClass aClass, consulo.module.Module module) {
        SpringJamElement javaSpringConfiguration = getJavaConfiguration(aClass, module, JavaSpringConfigurationElement.META.getJamKey());
        if (javaSpringConfiguration != null) {
            return javaSpringConfiguration;
        }

        javaSpringConfiguration = getJavaConfiguration(aClass, module, SpringBootConfigurationElement.META.getJamKey());
        if (javaSpringConfiguration != null) {
            return javaSpringConfiguration;
        }

        return null;
    }

    private <T extends SpringJamElement> T getJavaConfiguration(PsiClass aClass, consulo.module.Module module, SemKey<T> jamKey) {
        return JamService.getJamService(module.getProject()).getJamElement(jamKey, aClass);
    }

    protected abstract void checkJavaConfiguration(
        SpringJamElement javaConfiguration,
        Module module,
        ProblemsHolder holder
    );
}
