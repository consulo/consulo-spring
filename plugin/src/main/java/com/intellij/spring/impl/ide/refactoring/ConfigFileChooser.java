package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.tree.SimpleNode;
import consulo.ui.ex.awt.tree.SimpleTree;
import consulo.ui.ex.awt.tree.SimpleTreeBuilder;
import consulo.ui.ex.awt.tree.SimpleTreeStructure;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.StandardFileSystems;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomService;

import jakarta.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class ConfigFileChooser extends DialogWrapper {
  private SimpleTree myTree;
  private JPanel myPanel;

  @RequiredReadAction
  public ConfigFileChooser(Project project, final PsiFile fileToIgnore) {
    super(project, false);
    setTitle(SpringLocalize.chooseConfigurationFile());

    final List<DomFileElement<Beans>> list =
      DomService.getInstance().getFileElements(Beans.class, project, GlobalSearchScope.allScope(project));
    consulo.module.Module[] modules = ModuleManager.getInstance(project).getModules();
    final List<SpringModel> springModels = new ArrayList<>();
    for (Module module : modules) {
      springModels.addAll(SpringManager.getInstance(project).getAllModels(module));
    }
    SimpleTreeStructure structure = new SimpleTreeStructure() {
      @Override
      public Object getRootElement() {
        return new SimpleNode() {
          @Override
          @RequiredReadAction
          public SimpleNode[] getChildren() {
            List<XmlFile> files = new ArrayList<>(list.size());
            for (DomFileElement<Beans> element : list) {
              files.add(element.getFile());
            }
            if (fileToIgnore instanceof XmlFile) {
              files.remove(fileToIgnore);
            }
            List<SimpleNode> nodes = new ArrayList<>();
            for (SpringModel springModel : springModels) {
              nodes.add(new FileSetNode(springModel));
              for (XmlFile file : springModel.getConfigFiles()) {
                files.remove(file);
              }
            }
            for (XmlFile file : files) {
              VirtualFile vFile = file.getVirtualFile();
              if (vFile != null && (!vFile.getPath().contains(StandardFileSystems.JAR_SEPARATOR) || ArchiveVfsUtil.getVirtualFileForArchive(vFile) == null)) {
                nodes.add(new ConfigFileNode(file));
              }
            }
            return nodes.toArray(new SimpleNode[nodes.size()]);
          }

          @Override
          public boolean isAutoExpandNode() {
            return true;
          }
        };
      }
    };
    myTree.setRootVisible(false);
    SimpleTreeBuilder builder = new SimpleTreeBuilder(myTree, (DefaultTreeModel)myTree.getModel(), structure, null);
    builder.initRoot();

    init();

    builder.updateFromRoot();

    myTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && getSelectedFile() != null) {
          doOKAction();
        }
      }
    });

    myTree.requestFocus();
  }

  @Nullable
  public XmlFile getSelectedFile() {
    return myTree.getSelectedNode() instanceof ConfigFileNode configFileNode ? configFileNode.myFile : null;
  }

  @Override
  public boolean isOKActionEnabled() {
    return getSelectedFile() != null;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  @Override
  @RequiredUIAccess
  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  @Override
  protected String getDimensionServiceKey() {
    return "spring config file chooser";
  }

  private static class FileSetNode extends SimpleNode {
    private final SpringModel myModel;

    private FileSetNode(SpringModel model) {
      myModel = model;
      if (model.getFileSet() != null) {
        SpringFileSet springFileSet = model.getFileSet();
        setPlainText(springFileSet.getName());
        setIcon(springFileSet.getIcon());
      }
      else {
        setPlainText(model.getId());
        setIcon(SpringImplIconGroup.fileset());
      }
    }

    @Override
    public boolean isAutoExpandNode() {
      return true;
    }

    @Override
    public SimpleNode[] getChildren() {
      Set<XmlFile> files = myModel.getConfigFiles();
      return ContainerUtil.map2Array(files, SimpleNode.class, ConfigFileNode::new);
    }
  }

  private static class ConfigFileNode extends SimpleNode {
    private final XmlFile myFile;

    @RequiredReadAction
    private ConfigFileNode(XmlFile file) {
      myFile = file;
      setIcon(IconDescriptorUpdaters.getIcon(file, 0));
      addColoredFragment(myFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      VirtualFile virtualFile = myFile.getVirtualFile();
      assert virtualFile != null;
      addColoredFragment(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
    }

    @Override
    public SimpleNode[] getChildren() {
      return new SimpleNode[0];
    }
  }
}
