/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopIntroduction;
import com.intellij.aop.IntroductionManipulator;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.editor.XmlSuppressableInspectionTool;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.language.psi.XmlElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
@ExtensionImpl
public class DeclareParentsInspection extends XmlSuppressableInspectionTool {
    private static final Logger LOG = Logger.getInstance(DeclareParentsInspection.class);

    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return AopLocalize.inspectionDisplayNameDeclareparents();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "DeclareParentsInspection";
    }

    @Nonnull
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PsiLiteralExpression || element instanceof XmlAttributeValue) {
                    PsiElement injectedElement =
                        InjectedLanguageManager.getInstance(holder.getProject()).findElementAtNoCommit(
                            element.getContainingFile(),
                            element.getTextRange().getStartOffset() + 1
                        );

                    PsiFile file = injectedElement == null ? null : injectedElement.getContainingFile();
                    if (file instanceof AopPointcutExpressionFile) {
                        final IntroductionManipulator manipulator =
                            ((AopPointcutExpressionFile) file).getAopModel().getIntroductionManipulator();
                        if (manipulator == null) {
                            return;
                        }
                        AopIntroduction introduction = manipulator.getIntroduction();
                        if (introduction == null) {
                            return;
                        }

                        PsiClass intf = introduction.getImplementInterface().getValue();
                        if (intf == null && introduction.getImplementInterface().getStringValue() != null
                            || intf != null && !intf.isInterface()) {
                            registerProblem(manipulator.getInterfaceElement(), AopLocalize.errorInterfaceExpected().get(), holder);
                            return;
                        }
                        if (intf == null) {
                            return;
                        }

                        PsiClass defaultImpl = introduction.getDefaultImpl().getValue();
                        if (defaultImpl == null) {
                            if (!(element instanceof XmlElement) && !ContainerUtil.findAll(
                                intf.getAllMethods(),
                                new Condition<PsiMethod>() {
                                    public boolean value(PsiMethod method) {
                                        return method.hasModifierProperty(PsiModifier.ABSTRACT);
                                    }
                                }
                            ).isEmpty()) {
                                holder.newProblem(AopLocalize.errorDefaultImplementationClassShouldBeSpecified())
                                    .range(manipulator.getCommonProblemElement())
                                    .withFix(new LocalQuickFix() {
                                        @Nonnull
                                        @Override
                                        public LocalizeValue getName() {
                                            return AopLocalize.quickfixNameDefineAttribute(manipulator.getDefaultImplAttributeName());
                                        }

                                        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                                            try {
                                                if (ReadonlyStatusHandler.getInstance(project)
                                                    .ensureFilesWritable(descriptor.getPsiElement()
                                                        .getContainingFile()
                                                        .getVirtualFile())
                                                    .hasReadonlyFiles()) {
                                                    return;
                                                }
                                                manipulator.defineDefaultImpl(project, descriptor);
                                            }
                                            catch (IncorrectOperationException e) {
                                                LOG.error(e);
                                            }
                                        }
                                    })
                                    .create();

                            }
                            return;
                        }
                        if (defaultImpl.hasModifierProperty(PsiModifier.ABSTRACT) || !defaultImpl.isInheritor(intf, true)) {
                            PsiElement defaultImplElement = manipulator.getDefaultImplElement();
                            assert defaultImplElement != null;
                            registerProblem(
                                defaultImplElement,
                                AopLocalize.errorNonAbstractClassImplemention0Expected(intf.getQualifiedName()).get(),
                                holder
                            );
                        }
                    }
                }
            }
        };
    }

    private static void registerProblem(PsiElement element, String descriptionTemplate, ProblemsHolder holder) {
        int startOffset = element.getTextRange().getStartOffset();
        int quotes = element.getText().startsWith("\"") ? 1 : 0;
        TextRange range = TextRange.from(quotes, Math.max(element.getTextLength() - 2 * quotes, 1));
        holder.registerProblem(holder.getManager().createProblemDescriptor(
            element,
            range,
            descriptionTemplate,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
        ));
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return AopLocalize.inspectionGroupDisplayNameAop();
    }
}