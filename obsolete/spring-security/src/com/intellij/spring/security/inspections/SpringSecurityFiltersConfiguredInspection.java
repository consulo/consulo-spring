package com.intellij.spring.security.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.DataManager;
import com.intellij.javaee.model.xml.web.Filter;
import com.intellij.javaee.model.xml.web.FilterMapping;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.highlighting.SpringBeanInspectionBase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.security.SpringSecurityBundle;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.util.SpringSecurityUtil;
import java.util.function.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Serega.Vasiliev
 */
public class SpringSecurityFiltersConfiguredInspection extends SpringBeanInspectionBase {


  public SpringSecurityFiltersConfiguredInspection() {
  }

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final Module module = domFileElement.getModule();
    if (module == null) {
      return;
    }
    final VirtualFile virtualFile = domFileElement.getFile().getVirtualFile();
    if (virtualFile == null) {
      return;
    }

    if (SpringSecurityUtil.isSpringSecurityUsed(domFileElement)) {
      Collection<WebFacet> webFacets = WebFacet.getInstances(module);
      if (webFacets.size() > 0 && !SpringSecurityUtil.isFilterConfigured(webFacets)) {
        holder
          .createProblem(domFileElement, HighlightSeverity.WARNING, SpringSecurityBundle.message("spring.security.filter.not.configured"),
                         new AddSecurityFilterInWebXmlFix(module));
      }
    }
  }


  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringSecurityBundle.message("filters.configuration.inspection");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringSecurityFiltersConfiguredInspection";
  }

  private static class AddSecurityFilterInWebXmlFix implements LocalQuickFix, IntentionAction {
    protected final Module myModule;

    protected AddSecurityFilterInWebXmlFix(@NotNull Module module) {
      myModule = module;
    }

    @NotNull
    public String getName() {
      return SpringSecurityBundle.message("add.security.filter", SpringSecurityClassesConstants.DELEGATING_FILTER_PROXY);
    }

    @NotNull
    public String getText() {
      return getName();
    }

    @NotNull
    public String getFamilyName() {
      return SpringBundle.message("model.bean.quickfix.family");
    }

    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return true;
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      doFix(project);
      DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
      DaemonCodeAnalyzer.getInstance(project).restart();
    }

    public boolean startInWriteAction() {
      return false;
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      doFix(project);
      DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
      DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected void doFix(final Project project) {
      Processor<WebApp> processor = new Processor<WebApp>() {
        public boolean process(final WebApp webApp) {
          new WriteCommandAction<WebApp>(project) {
            @Override
            protected void run(Result<WebApp> webAppResult) throws Throwable {
              if (webApp != null) {
                String filterName = suggestFilterName(webApp);

                Filter filter = webApp.addFilter();
                filter.getFilterName().setStringValue(filterName);
                filter.getFilterClass().setStringValue(SpringSecurityClassesConstants.DELEGATING_FILTER_PROXY);

                FilterMapping filterMapping = webApp.addFilterMapping();
                filterMapping.getFilterName().setStringValue(filterName);
                filterMapping.addUrlPattern().setStringValue("/*");
                SpringUtils.navigate(filter);
              }
            }

            private String suggestFilterName(WebApp webApp) {
              List<String> names = ContainerUtil.mapNotNull(webApp.getFilters(), new Function<Filter, String>() {
                public String fun(Filter filter) {
                  return filter.getFilterName().getStringValue();
                }
              });
              final String suggestedName = "filterChainProxy";
              String resultName = suggestedName;
              int index = 0;
              while (names.contains(resultName)) {
                resultName = suggestedName + (++index);
              }
              return resultName;
            }
          }.execute();
          return false;
        }
      };

      processWebApp(processor);
    }

    public void processWebApp(final Processor<WebApp> processor) {
      Collection<WebFacet> webFacets = WebFacet.getInstances(myModule);
      if (webFacets.size() == 0) return;
      if (webFacets.size() == 1) {
        processor.process(webFacets.iterator().next().getRoot());
      }
      else {
        final ArrayList<WebFacet> list = new ArrayList<WebFacet>(webFacets);

        final BaseListPopupStep<WebFacet> step = new BaseListPopupStep<WebFacet>(SpringSecurityBundle.message("choose.web.set"), list) {
          @Override
          public PopupStep onChosen(WebFacet selectedValue, boolean finalChoice) {
            processor.process(selectedValue.getRoot());
            return FINAL_CHOICE;
          }
        };

        JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(DataManager.getInstance().getDataContext());
      }
    }
  }
}