package consulo.spring.impl.context.widget;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import consulo.annotation.access.RequiredReadAction;
import consulo.dataContext.DataContext;
import consulo.fileEditor.statusBar.EditorBasedStatusBarPopup;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.event.DumbModeListener;
import consulo.project.ui.wm.StatusBarWidget;
import consulo.project.ui.wm.StatusBarWidgetFactory;
import consulo.ui.ex.popup.ListPopup;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-04-28
 */
public class SpringContextSetWidget extends EditorBasedStatusBarPopup {
  public SpringContextSetWidget(@Nonnull Project project, @Nonnull StatusBarWidgetFactory factory) {
    super(project, factory, false);

    project.getMessageBus().connect(this).subscribe(DumbModeListener.class, new DumbModeListener() {
      @Override
      public void enteredDumbMode() {
        update();
      }

      @Override
      public void exitDumbMode() {
        update();
      }
    });
  }

  @Nonnull
  @Override
  @RequiredReadAction
  protected WidgetState getWidgetState(@Nullable VirtualFile virtualFile) {
    if (virtualFile == null) {
      return WidgetState.HIDDEN;
    }

    Module module = ModuleUtilCore.findModuleForFile(virtualFile, myProject);
    if (module == null) {
      return WidgetState.HIDDEN;
    }

    SpringModel model = SpringManager.getInstance(myProject).getModel(module);
    if (model == null) {
      return WidgetState.HIDDEN;
    }

    WidgetState state = new WidgetState("Spring Context", model.getFileSet().getName(), true);
    state.setIcon(model.getFileSet().getIcon());
    return state;
  }

  @Nullable
  @Override
  protected ListPopup createPopup(DataContext dataContext) {
    return null;
  }

  @Nonnull
  @Override
  protected StatusBarWidget createInstance(@Nonnull Project project) {
    return new SpringContextSetWidget(project, myFactory);
  }
}
