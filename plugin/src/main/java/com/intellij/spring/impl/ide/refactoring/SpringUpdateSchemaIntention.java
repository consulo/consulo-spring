package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanScope;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ex.awt.Messages;
import consulo.xml.language.psi.*;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "spring.update.scheme", fileExtensions = "xml", categories = {"XML", "Spring"})
public class SpringUpdateSchemaIntention implements IntentionAction {
    private static final String BEANS = "beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\"";

    @Nonnull
    public LocalizeValue getText() {
        return LocalizeValue.localizeTODO(SpringBundle.message("update.schema.intention"));
    }

    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        if (file instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile) file)) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement psiElement = file.findElementAt(offset);
            if (PsiTreeUtil.getParentOfType(psiElement, XmlDoctype.class) != null) {
                return true;
            }
            XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
            if (tag != null && tag.getParentTag() == null && isUpdateNeeded((XmlFile) file)) {
                return true;
            }
        }
        return false;
    }

    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        updateSchema((XmlFile) file);
    }

    public static boolean requestSchemaUpdate(@Nonnull XmlFile file) throws IncorrectOperationException {
        if (!isUpdateNeeded(file)) {
            return true;
        }
        if (!ApplicationManager.getApplication().isUnitTestMode() &&
            Messages.showYesNoDialog(SpringBundle.message("xml.schema.will.be.updated"),
                SpringBundle.message("xml.schema.update.is.required"), Messages.getQuestionIcon()
            ) != 0) {
            return false;
        }
        updateSchema(file);
        return true;
    }

    public static void updateSchema(@Nonnull XmlFile file) throws IncorrectOperationException {

        Project project = file.getProject();
        XmlDocument document = file.getDocument();
        assert document != null;
        XmlProlog prolog = document.getProlog();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document doc = documentManager.getDocument(file);
        assert doc != null;
        if (prolog != null) {
            XmlDoctype doctype = prolog.getDoctype();
            if (doctype != null) {
                doctype.delete();
                documentManager.doPostponedOperationsAndUnblockDocument(doc);
            }
        }
        DomFileElement<Beans> element = DomManager.getDomManager(project).getFileElement(file, Beans.class);
        assert element != null;
        XmlTag tag = element.getRootTag();
        assert tag != null;
        ASTNode node = tag.getNode();
        assert node != null;
        ASTNode child = XmlChildRole.START_TAG_NAME_FINDER.findChild(node);
        assert child != null;
        TextRange range = child.getTextRange();
        doc.replaceString(range.getStartOffset(), range.getEndOffset(), BEANS);
        documentManager.commitDocument(doc);
        CodeStyleManager.getInstance(project).reformatRange(tag, 1, BEANS.length());

        SpringModelVisitor.visitBeans(new SpringModelVisitor() {
            protected boolean visitBean(CommonSpringBean bean) {
                if (bean instanceof SpringBean) {
                    Boolean value = ((SpringBean) bean).getSingleton().getValue();
                    if (value != null) {
                        ((SpringBean) bean).getSingleton().undefine();
                        ((SpringBean) bean).getScope()
                            .setValue(value.booleanValue() ? SpringBeanScope.SINGLETON_SCOPE : SpringBeanScope.PROROTYPE_SCOPE);
                    }
                }
                return true;
            }
        }, element.getRootElement());
    }

    public boolean startInWriteAction() {
        return true;
    }

    public static boolean isUpdateNeeded(@Nonnull XmlFile config) {
        XmlDocument document = config.getDocument();
        assert document != null;
        XmlTag tag = document.getRootTag();
        assert tag != null;
        return !tag.getNamespace().equals(SpringConstants.BEANS_XSD);
    }
}
