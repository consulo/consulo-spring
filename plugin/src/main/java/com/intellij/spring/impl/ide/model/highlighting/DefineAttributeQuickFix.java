/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class DefineAttributeQuickFix implements LocalQuickFix {
    private static final Logger LOG = Logger.getInstance(DefineAttributeQuickFix.class);
    private final String myAttrName;

    public DefineAttributeQuickFix(String attrName) {
        myAttrName = attrName;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return SpringLocalize.aopQuickfixDefine0Attr(myAttrName);
    }

    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        try {
            XmlTag tag = (XmlTag) descriptor.getPsiElement();
            if (ReadonlyStatusHandler.getInstance(project)
                .ensureFilesWritable(descriptor.getPsiElement().getContainingFile().getVirtualFile())
                .hasReadonlyFiles()) {
                return;
            }
            XmlAttribute attribute = tag.setAttribute(myAttrName, "", "");

            OpenFileDescriptorFactory.getInstance(project)
                .builder(tag.getContainingFile().getVirtualFile())
                .offset(attribute.getValueElement().getTextRange().getStartOffset() + 1)
                .build()
                .navigate(true);
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }
}
