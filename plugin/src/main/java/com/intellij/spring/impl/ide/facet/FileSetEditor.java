package com.intellij.spring.impl.ide.facet;

import com.intellij.spring.impl.ide.SpringBundle;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.IdeaFileChooser;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.tree.CheckedTreeNode;
import consulo.ui.ex.awt.tree.TreeModelAdapter;
import consulo.ui.ex.awt.tree.TreeUtil;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public class FileSetEditor extends DialogWrapper {

  public static final DefaultListCellRenderer FILESET_RENDERER = new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
      if (value == null) {
        value = SpringBundle.message("fileset.none");
      }
      else if (((SpringFileSet)value).isNew()) {
        value = SpringBundle.message("fileset.new");
      }

      return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  };

  private JPanel myMainPanel;
  private EditorTextField mySetName;
  private SpringFilesTree myFilesTree;
  private JComboBox myParentBox;

  private final SpringFileSet myFileSet;
  private final CheckedTreeNode myRoot = new CheckedTreeNode(null);
  private final SpringFileSet myOriginalSet;

  public FileSetEditor(@Nonnull consulo.module.Module module, SpringFileSet fileSet, Set<SpringFileSet> allSets) {
    super(module.getProject(), true);

    myOriginalSet = fileSet;
    myFileSet = new XmlSpringFileSet(fileSet);

    init(fileSet, allSets, new SpringConfigsSearcher(module), module.getProject());
  }

  protected FileSetEditor(Component parent,
                          SpringFileSet fileSet,
                          Collection<SpringFileSet> allSets,
                          SpringConfigsSearcher searcher,
                          Project project) {

    super(parent, true);

    myOriginalSet = fileSet;
    myFileSet = new XmlSpringFileSet(fileSet);

    init(fileSet, allSets, searcher, project);
  }

  private void init(SpringFileSet fileSet,
                    Collection<SpringFileSet> allSets,
                    SpringConfigsSearcher searcher,
                    @Nullable Project project) {

    setTitle(SpringBundle.message("config.fileset.editor.title"));
    myFilesTree.setModel(new DefaultTreeModel(myRoot));
    searcher.search();
    MultiMap<Module, PsiFile> files = searcher.getFilesByModules();
    MultiMap<VirtualFile, PsiFile> jars = searcher.getJars();
    Set<PsiFile> psiFiles = myFilesTree.buildModuleNodes(files, jars, fileSet);
    List<VirtualFile> virtualFiles = searcher.getVirtualFiles();

    for (VirtualFile virtualFile : virtualFiles) {
      myFilesTree.addFile(virtualFile);
    }

    if (project != null) {
      PsiManager psiManager = PsiManager.getInstance(project);
      Collection<VirtualFilePointer> list = fileSet.getFiles();
      for (VirtualFilePointer pointer : list) {
        VirtualFile file = pointer.getFile();
        if (file != null) {
          PsiFile psiFile = psiManager.findFile(file);
          if (psiFile != null && psiFiles.contains(psiFile)) {
            continue;
          }
          myFilesTree.addFile(file);
        }
      }
    }

    TreeUtil.expandAll(myFilesTree);
    myFilesTree.getModel().addTreeModelListener(new TreeModelAdapter() {
      @Override
      public void treeNodesChanged(TreeModelEvent e) {
        updateFileSet();
      }
    });

    mySetName.setText(fileSet.getName());
    mySetName.addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        updateFileSet();
      }
    });

    for (SpringFileSet set : allSets) {
      if (set.getId().equals(fileSet.getId())) {
        continue;
      }
      myParentBox.addItem(set);
    }
    myParentBox.addItem(null);
    myParentBox.setSelectedItem(myFileSet.getDependencies().size() > 0 ? myFileSet.getDependencies().get(0) : null);

    myParentBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateFileSet();
      }
    });
    myParentBox.setRenderer(FILESET_RENDERER);
    init();

    getOKAction().setEnabled(fileSet.isNew());
  }

  @Override
  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  @NonNls
  protected String getDimensionServiceKey() {
    return "spring file set editor";
  }

  @Override
  public boolean isOKActionEnabled() {
    if (myOriginalSet.isNew()) {
      return true;
    }

    if (myFileSet.getFiles().size() != myOriginalSet.getFiles().size()) {
      return true;
    }
    List<VirtualFilePointer> pointers = new ArrayList<>(myFileSet.getFiles());
    List<VirtualFilePointer> originalPointers = new ArrayList<>(myOriginalSet.getFiles());
    for (int i = 0; i < pointers.size(); i++) {
      if (!pointers.get(i).getUrl().equals(originalPointers.get(i).getUrl())) {
        return true;
      }
    }
    boolean b = myFileSet.getDependencies().equals(myOriginalSet.getDependencies());
    return !myFileSet.getName().equals(myOriginalSet.getName()) || !b;
  }

  @Override
  protected void doOKAction() {
    updateFileSet();
    super.doOKAction();
  }

  private void updateFileSet() {
    myFileSet.setName(mySetName.getText());
    myFilesTree.updateFileSet(myFileSet);
    SpringFileSet parent = (SpringFileSet)myParentBox.getSelectedItem();
    myFileSet.setDependencies(parent == null ? Collections.<String>emptyList() : Arrays.asList(parent.getId()));
    getOKAction().setEnabled(isOKActionEnabled());
  }

  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Override
  protected Action[] createLeftSideActions() {
    AbstractAction locateAction = new AbstractAction(SpringBundle.message("config.locate.button")) {
      @Override
      public void actionPerformed(ActionEvent e) {
        VirtualFile[] files =
          IdeaFileChooser.chooseFiles(new FileChooserDescriptor(true, false, true, false, true, true), myMainPanel, null, null);
        if (files.length > 0) {
          for (VirtualFile file : files) {
            myFilesTree.addFile(file);
          }
          updateFileSet();
          TreeUtil.expandAll(myFilesTree);
        }
      }
    };
    return new Action[]{locateAction};
  }

  public SpringFileSet getEditedFileSet() {
    return myFileSet;
  }
}
