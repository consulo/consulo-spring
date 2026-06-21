package consulo.spring.impl.toolWindow;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.messagebus.MessageBusConnection;
import consulo.dataContext.DataProvider;
import consulo.dataContext.DataSink;
import consulo.dataContext.UiDataProvider;
import consulo.disposer.Disposable;
import consulo.module.Module;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.module.event.ModuleAdapter;
import consulo.module.event.ModuleListener;
import consulo.project.Project;
import consulo.spring.impl.toolWindow.tree.node.SpringContextsTreeStructure;
import consulo.ui.ex.awt.ScrollPaneFactory;
import consulo.ui.ex.awt.tree.AsyncTreeModel;
import consulo.ui.ex.awt.tree.StructureTreeModel;
import consulo.ui.ex.awt.tree.Tree;
import consulo.ui.ex.awt.tree.TreeUtil;
import consulo.ui.ex.tree.NodeDescriptor;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author VISTALL
 * @since 2024-12-13
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class SpringToolWindowPanel implements Disposable, UiDataProvider {
    public static final Key<SpringToolWindowPanel> KEY = Key.of(SpringToolWindowPanel.class);

    private final Tree myTree;
    private final Project myProject;
    private JPanel myRootPanel;
    private final StructureTreeModel<SpringContextsTreeStructure> myTreeModel;
    private final SpringContextsTreeStructure myStructure;

    @Inject
    public SpringToolWindowPanel(Project project) {
        myProject = project;
        myRootPanel = new JPanel(new BorderLayout());

        myStructure = new SpringContextsTreeStructure(project);
        myTreeModel = new StructureTreeModel<>(myStructure, this);
        myTree = new Tree(new AsyncTreeModel(myTreeModel, this)) {
            @Override
            public final int getToggleClickCount() {
                int count = super.getToggleClickCount();
                TreePath path = getSelectionPath();
                if (path != null) {
                    NodeDescriptor descriptor = TreeUtil.getLastUserObject(NodeDescriptor.class, path);
                    if (descriptor != null && !descriptor.expandOnDoubleClick()) {
                        return -1;
                    }
                }
                return count;
            }
        };
        myTree.setRootVisible(false);
        myRootPanel.add(ScrollPaneFactory.createScrollPane(myTree, true));

        MessageBusConnection connection = project.getMessageBus().connect(this);

        connection.subscribe(ModuleRootListener.class, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                invalidate();
            }
        });

        connection.subscribe(ModuleListener.class, new ModuleAdapter() {
            @Override
            public void moduleAdded(Project project, Module module) {
                invalidate();
            }

            @Override
            public void moduleRemoved(Project project, Module module) {
                invalidate();
            }

            @Override
            public void modulesRenamed(Project project, List<Module> modules) {
                invalidate();
            }
        });
    }

    protected void invalidate() {
        myProject.getUIAccess().getScheduler().schedule(() -> {
            myTreeModel.invalidate(myStructure.getRootElement(), true);
        }, 1, TimeUnit.SECONDS);
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    @Override
    public void uiDataSnapshot(DataSink sink) {
        sink.set(KEY, this);
    }

    @Override
    public void dispose() {

    }
}
