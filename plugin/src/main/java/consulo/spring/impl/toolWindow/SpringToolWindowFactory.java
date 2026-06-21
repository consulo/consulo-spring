package consulo.spring.impl.toolWindow;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
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
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2024-12-13
 */
@ExtensionImpl
public class SpringToolWindowFactory implements ToolWindowFactory, DumbAware {
    public static final String ID = "spring";

    @Nonnull
    @Override
    public String getId() {
        return ID;
    }

    @RequiredUIAccess
    @Override
    public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();

        ContentFactory factory = contentManager.getFactory();

        SpringToolWindowPanel panel = project.getInstance(SpringToolWindowPanel.class);

        Content content = factory.createContent(panel.getRootPanel(), null, false);

        contentManager.addContent(content);

        contentManager.addUiDataProvider(panel);
    }

    @Override
    public boolean validate(@Nonnull Project project) {
        return ModuleExtensionHelper.getInstance(project).hasModuleExtension(SpringModuleExtension.class);
    }

    @Override
    public boolean isSecondary() {
        return true;
    }

    @Nonnull
    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return SpringImplIconGroup.springtoolwindow();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Spring");
    }
}
