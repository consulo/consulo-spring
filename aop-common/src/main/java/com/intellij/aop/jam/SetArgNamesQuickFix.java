/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.ArgNamesManipulator;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.impl.psi.CheckUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class SetArgNamesQuickFix implements LocalQuickFix {
    private static final Logger LOG = Logger.getInstance("#com.intellij.aop.jam.SetArgNamesQuickFix");
    @Nonnull
    private final LocalizeValue myName;
    private final boolean mySet;
    private final ArgNamesManipulator myManipulator;
    private final PsiMethod myMethod;

    public SetArgNamesQuickFix(@Nonnull LocalizeValue name, boolean set, ArgNamesManipulator manipulator, PsiMethod method) {
        myName = name;
        mySet = set;
        myManipulator = manipulator;
        myMethod = method;
    }

    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        try {
            CheckUtil.checkWritable(descriptor.getPsiElement());
            if (mySet) {
                PsiMethod method = myMethod;
                StringBuilder result = new StringBuilder();
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    String name = parameter.getName();
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    result.append(name);
                }
                myManipulator.setArgNames(result.toString());
            }
            else {
                myManipulator.setArgNames(null);
            }
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return myName;
    }
}
