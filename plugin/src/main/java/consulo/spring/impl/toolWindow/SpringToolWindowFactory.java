package consulo.spring.impl.toolWindow;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentFactory;
import consulo.ui.ex.content.ContentManager;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;
import consulo.application.dumb.DumbAware;

/**
 * @author VISTALL
 * @since 2024-12-13
 */
@ExtensionImpl
public class SpringToolWindowFactory implements ToolWindowFactory, DumbAware {
    public static final String ID = "spring";

    @Override
    public String getId() {
        return ID;
    }

    @RequiredUIAccess
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        SpringBeansView view = new SpringBeansView(project);

        Content content = ContentFactory.getInstance().createUIContent(view.getComponent(), "", false);
        content.setCloseable(false);
        content.setDisposer(view);

        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addUiDataProvider(view);
        contentManager.addContent(content);
    }

    @Override
    public boolean validate(Project project) {
        return ModuleExtensionHelper.getInstance(project).hasModuleExtension(SpringModuleExtension.class);
    }

    @Override
    public boolean isSecondary() {
        return true;
    }

    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Override
    public Image getIcon() {
        return SpringImplIconGroup.springtoolwindow();
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Spring");
    }
}
