package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import com.intellij.xml.XmlNamespaceHelper;
import com.intellij.xml.util.XmlUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlElementFactory;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomUtil;
import jakarta.annotation.Nonnull;

import java.util.Collections;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "spring.use.pname", fileExtensions = "xml", categories = {"XML", "Spring"})
public class UsePNamespaceIntention implements IntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return SpringLocalize.usePNamespace();
    }

    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof XmlFile)) {
            return false;
        }

        SpringProperty property = DomUtil.getContextElement(editor, SpringProperty.class);
        return property != null &&
            property.getParent() instanceof SpringBean &&
            property.getName().getStringValue() != null &&
            (DomUtil.hasXml(property.getValueElement()) || DomUtil.hasXml(property.getRefElement()));
    }

    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        if (SpringUpdateSchemaIntention.requestSchemaUpdate((XmlFile) file)) {
            SpringProperty property = DomUtil.getContextElement(editor, SpringProperty.class);
            assert property != null;
            if (property.getXmlTag().getNamespaceByPrefix("p").equals(XmlUtil.EMPTY_URI)) {
                XmlNamespaceHelper.getHelper((XmlFile) file).insertNamespaceDeclaration(
                    (XmlFile) file,
                    editor,
                    Collections.<String>singleton(SpringConstants.P_NAMESPACE),
                    "p",
                    null
                );
            }
            SpringBean bean = (SpringBean) property.getParent();
            assert bean != null;
            String name = property.getName().getStringValue();
            String value = property.getValueElement().getStringValue();
            XmlTag tag = bean.getXmlTag();
            if (value != null) {
                property.undefine();
                XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("p:" + name, value);
                tag.add(attribute);
                tag.collapseIfEmpty();
                return;
            }
            String ref = property.getRefElement().getStringValue();
            if (ref != null) {
                property.undefine();
                XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("p:" + name + "-ref", ref);
                tag.add(attribute);
                tag.collapseIfEmpty();
            }
        }
    }

    public boolean startInWriteAction() {
        return true;
    }
}
