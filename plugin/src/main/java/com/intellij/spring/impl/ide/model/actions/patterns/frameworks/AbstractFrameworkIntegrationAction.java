package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.ChooseTemplatesDialogWrapper;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.Result;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.document.util.TextRange;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.event.TemplateEditingAdapter;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nullable;

import java.util.List;

public abstract class AbstractFrameworkIntegrationAction extends FrameworkIntegrationAction {
  @Override
  @RequiredUIAccess
  protected void generateSpringBeans(final consulo.module.Module module, final Editor editor, final XmlFile xmlFile) {
    final ChooseTemplatesDialogWrapper dialogWrapper =
      new ChooseTemplatesDialogWrapper(module.getProject(), getTemplateInfos(module), getLibrariesInfo(module), getDescription().get());

    dialogWrapper.show();

    if (dialogWrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(xmlFile.getProject()).ensureFilesWritable(xmlFile.getVirtualFile());
      if (status.hasReadonlyFiles()) return;

      new WriteCommandAction(module.getProject()) {
        @Override
        protected void run(Result result) throws Throwable {
          addFacet(module);
          dialogWrapper.getTemplatesForm().getLibrariesValidationComponent().setupLibraries();

          moveCaretIfNeeded(module.getProject(), editor, xmlFile);

          runTemplates(module.getProject(), editor, dialogWrapper.getSelectedTemplates(), 0);
        }
      }.execute();
    }
  }

  protected void addFacet(final consulo.module.Module module) {
    if (module == null) return;

    final String facetId = getFacetId();
    if (!StringUtil.isEmptyOrSpaces(facetId)) {
      /*
      TODO [VISTALL]
      final FacetManager facetManager = FacetManager.getInstance(module);
      final FacetType<?,?> type = FacetTypeRegistry.getInstance().findFacetType(facetId);

      if (type != null) {

        if (facetManager.getFacetByType(type.getId()) == null) {
          final ModifiableFacetModel model = facetManager.createModifiableModel();

          final Facet facet = facetManager.addFacet(type, type.getDefaultFacetName(), null);

          model.addFacet(facet);
          model.commit();
        }
      } */
    }
  }

  @Nullable
  protected String getFacetId() {
    return null;
  }

  protected abstract LibrariesInfo getLibrariesInfo(final Module module);

  protected abstract List<TemplateInfo> getTemplateInfos(final consulo.module.Module module);

  private static void moveCaretIfNeeded(final Project project, final Editor editor, final XmlFile xmlFile) {
    final SpringBean bean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, xmlFile);
    if (bean != null) {
      SpringBean springBean = SpringUtils.getTopLevelBean(bean);

      if (springBean.getXmlTag() != null) {
        final TextRange range = springBean.getXmlTag().getTextRange();
        int offset = range.getEndOffset();
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
      }
    }
  }

  private static void runTemplates(final Project project, final Editor editor, final List<Template> templates, final int index) {
    Template template = templates.get(index);
    template.setToReformat(true);

    TemplateManager.getInstance(project).startTemplate(editor, template, new TemplateEditingAdapter() {
      @Override
      public void templateFinished(Template template, boolean brokenOff) {
        if (index + 1 < templates.size()) {
          Application.get().invokeLater(() -> new WriteCommandAction(project) {
            @Override
            protected void run(Result result) throws Throwable {
              runTemplates(project, editor, templates, index + 1);
            }
          }.execute());
        }
      }
    });
  }
}
