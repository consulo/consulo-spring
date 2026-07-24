/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 15, 2006
 * Time: 1:06:27 PM
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.aop.psi.*;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.xml.aop.Advisor;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.model.aop.psi.SpringAopCompletionContributor;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.editor.BasicDomElementsInspection;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomHighlightingHelper;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.List;

@ExtensionImpl
public class SpringAopErrorsInspection extends BasicDomElementsInspection<Beans, Object> {

    public SpringAopErrorsInspection() {
        super(Beans.class);
    }

    protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
        XmlTag tag = element.getXmlTag();
        if (element instanceof BasicAdvice && tag != null) {
            BasicAdvice advice = (BasicAdvice) element;
            if (advice.getPointcut().getXmlAttribute() == null && advice.getPointcutRef().getXmlAttribute() == null) {
                createPointcutProblem(element, holder);
            }
        }
        else if (element instanceof Advisor && tag != null) {
            Advisor advisor = (Advisor) element;
            if (advisor.getPointcut().getXmlAttribute() == null && advisor.getPointcutRef().getXmlAttribute() == null) {
                createPointcutProblem(element, holder);
            }
        }
    }

    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull final InspectionManager manager, boolean isOnTheFly) {
        if (file instanceof AopPointcutExpressionFile) {
            final List<ProblemDescriptor> result = new SmartList<ProblemDescriptor>();
            PsiPointcutExpression expression = ((AopPointcutExpressionFile) file).getPointcutExpression();
            if (expression != null) {
                expression.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (!(element instanceof PsiPointcutExpression)) {
                            return;
                        }
                        super.visitElement(element);
                        PsiElement firstChild = element.getFirstChild();
                        if (element instanceof PsiPointcutReferenceExpression || element instanceof AopNotExpression ||
                            element instanceof AopBinaryExpression || element instanceof AopParenthesizedExpression) {
                            return;
                        }
                        if (firstChild == null || firstChild.getFirstChild() != null) {
                            return;
                        }

                        @NonNls String text = firstChild.getText();
                        if (StringUtil.isEmptyOrSpaces(text)) {
                            return;
                        }

                        if (!Arrays.asList(SpringAopCompletionContributor.SPRING20_AOP_POINTCUTS).contains(text) && !"bean".equals(text)) {
                            result.add(manager.createProblemDescriptor(
                                firstChild,
                                SpringLocalize.thisPointcutDesignatorIsnTSupportedBySpring(text).get(),
                                LocalQuickFix.EMPTY_ARRAY,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                            ));
                        }
                    }
                });
            }
            return result.toArray(new ProblemDescriptor[result.size()]);
        }

        return super.checkFile(file, manager, isOnTheFly);
    }

    private static void createPointcutProblem(DomElement element, DomElementAnnotationHolder holder) {
        holder.createProblem(element, HighlightSeverity.ERROR, SpringBundle.message("error.pointcut.or.pointcut.ref.should.be.defined"),
            new DefineAttributeQuickFix("pointcut"), new DefineAttributeQuickFix("pointcut-ref")
        );
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return AopLocalize.inspectionGroupDisplayNameAop();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.aopErrorsInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringAopErrorsInspection";
    }
}