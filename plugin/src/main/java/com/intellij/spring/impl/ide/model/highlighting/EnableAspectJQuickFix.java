/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.xml.util.XmlUtil;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomFileElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class EnableAspectJQuickFix implements LocalQuickFix {
    private static final Logger LOG = Logger.getInstance(EnableAspectJQuickFix.class);
    private final SpringModel myModel;

    public EnableAspectJQuickFix(SpringModel beans) {
        myModel = beans;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return SpringLocalize.aopEnableAspectjFixText();
    }

    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
        DomFileElement<Beans> fileElement = myModel.getRoots().get(0);
        if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(fileElement.getFile().getVirtualFile()).hasReadonlyFiles()) {
            return;
        }

        XmlTag root = fileElement.getRootElement().ensureTagExists();
        try {
            if (isSchemaStyle(root)) {
                addAspectjAutoproxy(root);
            }
            else {
                XmlTag childTag = root.createChildTag("bean", root.getNamespace(), "", false);
                childTag.setAttribute("class", SpringConstants.ASPECTJ_AUTOPROXY_BEAN_CLASS);
                root.add(childTag);
            }
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }

    public static boolean isSchemaStyle(@Nullable XmlTag root) {
        return root != null && root.getNamespace().contains("http://www.springframework.org/schema/");
    }

    public static void addAspectjAutoproxy(XmlTag root) {
        try {
            if (root.getPrefixByNamespace(SpringConstants.AOP_NAMESPACE) == null && root.getNamespaceByPrefix("aop") == XmlUtil.EMPTY_URI) {
                root.setAttribute("xmlns:aop", SpringConstants.AOP_NAMESPACE);
            }
            root.add(root.createChildTag(SpringConstants.ASPECTJ_AUTOPROXY, SpringConstants.AOP_NAMESPACE, "", false));
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }
}
