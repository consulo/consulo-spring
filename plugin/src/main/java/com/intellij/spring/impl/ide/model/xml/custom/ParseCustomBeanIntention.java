package com.intellij.spring.impl.ide.model.xml.custom;

import com.intellij.spring.impl.ide.CustomBeanInfo;
import com.intellij.spring.impl.ide.CustomBeanRegistry;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Editor;
import consulo.execution.unscramble.UnscrambleService;
import consulo.language.editor.DaemonCodeAnalyzer;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.platform.base.localize.CommonLocalize;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionsBundle;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomUtil;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author peter
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "spring.parse.custom.bean", fileExtensions = "xml", categories = {"XML", "Spring"})
public class ParseCustomBeanIntention implements IntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return SpringLocalize.parseCustomBeanIntention();
    }

    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        return DomUtil.findDomElement(file.findElementAt(editor.getCaretModel().getOffset()), DomSpringBean.class)
            instanceof CustomBeanWrapper;
    }

    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        CustomBeanWrapper wrapper =
            DomUtil.findDomElement(file.findElementAt(editor.getCaretModel().getOffset()), CustomBeanWrapper.class);
        assert wrapper != null;
        invokeCustomBeanParsers(project, Arrays.asList(wrapper.getXmlTag()));
    }

    @RequiredUIAccess
    public static void invokeCustomBeanParsers(final Project project, final Collection<XmlTag> tags) {
        final Ref<CustomBeanRegistry.ParseResult> ref = Ref.create(null);
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
            public void run() {
                ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    public void run() {
                        ref.set(CustomBeanRegistry.getInstance(project).parseBeans(tags));
                    }
                });
            }
        }, SpringLocalize.parsingCustomBean().get(), false, project);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                PsiModificationTracker.getInstance(project).incCounter();
            }
        });
        DaemonCodeAnalyzer.getInstance(project).restart();

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return; //the rest deals only with pretty feedback to user
        }

        CustomBeanRegistry.ParseResult result = ref.get();

        String message = result.getErrorMessage();
        if (message != null) {
            Messages.showErrorDialog(project, message, SpringLocalize.parseCustomBeanError().get());
            return;
        }

        String trace = result.getStackTrace();
        if (trace != null) {
            String shortTrace = trace;
            int i = 0;
            for (int j = 0; j < 10; j++) {
                if (i >= 0) {
                    i = trace.indexOf('\n', i + 1);
                }
            }
            if (i >= 0) {
                shortTrace = trace.substring(0, i) + "\n\t...";
            }

            int exitCode = Messages.showDialog(
                project,
                shortTrace,
                SpringLocalize.parseCustomBeanError().get(),
                new String[]{CommonLocalize.buttonOk().get(), ActionsBundle.message("action.Unscramble.text")},
                0,
                UIUtil.getErrorIcon()
            );
            if (exitCode == 1) {
                UnscrambleService unscrambleService = project.getInstance(UnscrambleService.class);
                unscrambleService.showAsync(trace);
            }
            return;
        }

        List<CustomBeanInfo> infos = result.getBeans();
        assert infos != null;
        String beansText = StringUtil.join(infos, it -> "  id = " + it.beanName + "; class = " + it.beanClassName, "\n");

        if (infos.size() == 1 && tags.size() == 1) {
            String idAttr = infos.get(0).idAttribute;
            if (idAttr != null) {
                XmlTag tag = tags.iterator().next();
                String ns = tag.getNamespace();
                String localName = tag.getLocalName();
                LocalizeValue inductMessage = SpringLocalize.parseTheseBeansInduct(beansText, ns, localName, idAttr);
                if (Messages.showDialog(
                    project,
                    inductMessage.get(),
                    SpringLocalize.parseCustomBeanSuccess().get(),
                    new String[]{CommonLocalize.buttonYes().get(), SpringLocalize.parseTheseBeansInductOnlyThis().get()},
                    1,
                    UIUtil.getInformationIcon()
                ) == 0) {
                    CustomBeanInfo beanInfo = new CustomBeanInfo(infos.get(0));
                    beanInfo.beanName = null;
                    CustomBeanRegistry.getInstance(project).addBeanPolicy(ns, localName, beanInfo);
                }
                return;
            }
        }

        LocalizeValue parsedMessage = !infos.isEmpty()
            ? SpringLocalize.parseTheseBeans(beansText)
            : result.hasInfrastructureBeans()
            ? SpringLocalize.parseOnlyInfrastructureBeans()
            : SpringLocalize.parseNoCustomBeans();
        Messages.showInfoMessage(project, parsedMessage.get(), SpringLocalize.parseCustomBeanSuccess().get());
    }

    public boolean startInWriteAction() {
        return false;
    }
}