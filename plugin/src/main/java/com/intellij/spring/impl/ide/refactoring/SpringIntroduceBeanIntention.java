package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValueHolder;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.template.*;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.util.collection.ContainerUtil;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlElementFactory;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomUtil;
import jakarta.annotation.Nonnull;

import java.util.function.Function;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "spring.introduce.bean", fileExtensions = "xml", categories = {"XML", "Spring"})
public class SpringIntroduceBeanIntention implements IntentionAction {
    private final static Logger LOG = Logger.getInstance(SpringIntroduceBeanIntention.class);

    @Nonnull
    @Override
    public LocalizeValue getText() {
        return SpringLocalize.introduceBeanIntention();
    }

    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        if (!(file instanceof XmlFile) || !SpringManager.getInstance(project).isSpringBeans((XmlFile) file)) {
            return false;
        }

        SpringBean springBean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
        return springBean != null && springBean.getParent() instanceof SpringValueHolder;
    }

    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        SpringBean springBean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
        moveToTheTopLevel(project, editor, springBean);
    }

    public static void moveToTheTopLevel(Project project, Editor editor, SpringBean springBean) {
        if (springBean == null) {
            return;
        }
        SpringBean topLevelBean = SpringUtils.getTopLevelBean(springBean);

        SpringBean newBean = DomUtil.addElementAfter(topLevelBean);
        newBean.copyFrom(springBean);

        String id = newBean.getId().getValue();
        if (id == null) {
            try {
                XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("id", "");
                XmlTag tag = newBean.getXmlTag();
                XmlAttribute[] attributes = tag.getAttributes();
                if (attributes.length > 0) {
                    tag.addBefore(attribute, attributes[0]);
                }
                else {
                    tag.add(attribute);
                }
            }
            catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }

        SpringValueHolder holder = (SpringValueHolder) springBean.getParent();
        assert holder != null;
        holder.getRefAttr().setStringValue(id == null ? "" : id);

        springBean.undefine();

        XmlTag tag = holder.getXmlTag();
        tag.collapseIfEmpty();

        if (id != null) {
            return;
        }

        SpringBean topLevelBeanCopy = topLevelBean.createStableCopy();
        SpringBean newBeanCopy = newBean.createStableCopy();
        SpringValueHolder holderCopy = holder.createStableCopy();

        Document document = editor.getDocument();
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);

        int start = topLevelBeanCopy.getXmlTag().getTextOffset();
        int end = newBeanCopy.getXmlTag().getTextRange().getEndOffset();

        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        String text = document.getText();
        int refOffset = holderCopy.getRefAttr().getXmlAttributeValue().getTextOffset();
        int idOffset = newBeanCopy.getId().getXmlAttributeValue().getTextOffset();

        template.addTextSegment(text.substring(start, refOffset));

        final String[] names = SpringUtils.suggestBeanNames(newBean);
        Expression node = new Expression() {
            public Result calculateResult(ExpressionContext context) {
                return null;
            }

            public Result calculateQuickResult(ExpressionContext context) {
                return null;
            }

            public LookupElement[] calculateLookupItems(ExpressionContext context) {
                return ContainerUtil.map2Array(names, LookupElement.class, (Function<String, LookupElement>) LookupElementBuilder::create);
            }
        };
        template.addVariable("id", node, node, true);
        template.addTextSegment(text.substring(refOffset, idOffset));
        template.addVariableSegment("id");
        template.addTextSegment(text.substring(idOffset, end));

        document.deleteString(start, end);

        templateManager.startTemplate(editor, template);
    }

    public boolean startInWriteAction() {
        return true;
    }
}
