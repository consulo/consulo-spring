/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.facet;

import consulo.application.AllIcons;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiFile;
import consulo.module.Module;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.tree.*;
import consulo.ui.image.Image;
import consulo.util.collection.MultiMap;
import consulo.virtualFileSystem.StandardFileSystems;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.xml.psi.xml.XmlFile;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;
import java.util.function.Function;

/**
 * @author Dmitry Avdeev
*/
public class SpringFilesTree extends CheckboxTreeBase {
  
  private static final Comparator<PsiFile> FILE_COMPARATOR = new Comparator<PsiFile>() {
    public int compare(final PsiFile o1, final PsiFile o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  public SpringFilesTree() {
    super(new CheckboxTreeCellRendererBase() {
      public void customizeCellRenderer(final JTree tree,
                                        final Object value,
                                        final boolean selected,
                                        final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

        final ColoredTreeCellRenderer renderer = getTextRenderer();
        final Object object = ((CheckedTreeNode)value).getUserObject();
        if (object instanceof consulo.module.Module) {
          final consulo.module.Module module = (consulo.module.Module)object;
          renderer.setIcon(AllIcons.Nodes.Module);
          final String moduleName = module.getName();
          renderer.append(moduleName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else if (object instanceof PsiFile) {
          final PsiFile psiFile = (PsiFile)object;
          final Image icon = IconDescriptorUpdaters.getIcon(psiFile, 0);
          renderer.setIcon(icon);
          final String fileName = psiFile.getName();
          renderer.append(fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
          final VirtualFile virtualFile = psiFile.getVirtualFile();
          if (virtualFile != null) {
            String path = virtualFile.getPath();
            final int i = path.indexOf(StandardFileSystems.JAR_SEPARATOR);
            if (i >= 0) {
              path = path.substring(i + StandardFileSystems.JAR_SEPARATOR.length());
            }
            renderer.append(" (" + path + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
          }
        } else if (object instanceof VirtualFile) {
          VirtualFile file = (VirtualFile)object;
          renderer.setIcon(VirtualFileManager.getInstance().getFileIcon(file, null, 0));
          renderer.append(file.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
          String path = file.getPath();
          final int i = path.indexOf(StandardFileSystems.JAR_SEPARATOR);
          if (i >= 0) {
            path = path.substring(i + StandardFileSystems.JAR_SEPARATOR.length());
          }
          renderer.append(" (" + path + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
        }
      }
    }, null);

    TreeUIHelper.getInstance().installTreeSpeedSearch(this, new Function<TreePath, String>() {
      public String apply(final TreePath treePath) {
        final Object object = ((CheckedTreeNode)treePath.getLastPathComponent()).getUserObject();
        if (object instanceof consulo.module.Module) {
          return ((Module)object).getName();
        } else if (object instanceof PsiFile) {
          return ((PsiFile)object).getName();
        } else if (object instanceof VirtualFile) {
          return ((VirtualFile)object).getName();
        } else {
          return "";
        }
      }
    }, true);
  }

  public Set<PsiFile> buildModuleNodes(final MultiMap<Module,PsiFile> files,
															final MultiMap<VirtualFile, PsiFile> jars,
															final SpringFileSet fileSet) {

    final CheckedTreeNode root = (CheckedTreeNode)getModel().getRoot();
    final HashSet<PsiFile> psiFiles = new HashSet<PsiFile>();
    final List<consulo.module.Module> modules = new ArrayList<consulo.module.Module>(files.keySet());
    Collections.sort(modules, new Comparator<consulo.module.Module>() {
      public int compare(final consulo.module.Module o1, final consulo.module.Module o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (consulo.module.Module module: modules) {
      CheckedTreeNode moduleNode = new CheckedTreeNode(module);
      moduleNode.setChecked(false);
      root.add(moduleNode);
      if (files.containsKey(module)) {
        List<PsiFile> moduleFiles = new ArrayList<PsiFile>(files.get(module));
        Collections.sort(moduleFiles, FILE_COMPARATOR);
        for (PsiFile file: moduleFiles) {
          final CheckedTreeNode fileNode = createFileNode(file, fileSet);
          moduleNode.add(fileNode);
          psiFiles.add(file);
        }
      }
    }
    for (VirtualFile file: jars.keySet()) {
      final List<PsiFile> list = new ArrayList<PsiFile>(jars.get(file));
      final PsiFile jar = list.get(0).getManager().findFile(file);
      if (jar != null) {
        final CheckedTreeNode jarNode = new CheckedTreeNode(jar);
        jarNode.setChecked(false);
        root.add(jarNode);
        Collections.sort(list, FILE_COMPARATOR);
        for (PsiFile psiFile: list) {
          final CheckedTreeNode vfNode = createFileNode(psiFile, fileSet);
          jarNode.add(vfNode);
          psiFiles.add(psiFile);
        }
      }
    }
    return psiFiles;
  }

  public void updateFileSet(final SpringFileSet fileSet) {
    
    final boolean[] result = new boolean[] { false };
    final Set<VirtualFile> configured = new HashSet<VirtualFile>();
    TreeUtil.traverse((TreeNode)getModel().getRoot(), new TreeUtil.Traverse() {
      public boolean accept(Object node) {
        CheckedTreeNode checkedTreeNode = (CheckedTreeNode)node;
        if (!checkedTreeNode.isChecked()) {
          return true;
        }
        final Object object = checkedTreeNode.getUserObject();
        VirtualFile virtualFile = null;
        if (object instanceof XmlFile) {
          virtualFile = ((XmlFile)object).getVirtualFile();
        } else if (object instanceof VirtualFile) {
          virtualFile = (VirtualFile)object;
        }
        if (virtualFile != null) {
          if (!fileSet.hasFile(virtualFile)) {
            result[0] = true;
            fileSet.addFile(virtualFile);
          }
          configured.add(virtualFile);
        }
        return true;
      }
    });

    for (Iterator<VirtualFilePointer> i = fileSet.getFiles().iterator(); i.hasNext();) {
      final VirtualFilePointer pointer = i.next();
      final VirtualFile file = pointer.getFile();
      if (file == null || !configured.contains(file)) {
        result[0] = true;
        i.remove();
      }
    }
  }

  private static CheckedTreeNode createFileNode(PsiFile file, SpringFileSet fileSet) {
    final CheckedTreeNode fileNode = new CheckedTreeNode(file);
    fileNode.setChecked(fileSet.hasFile(file.getVirtualFile()));
    return fileNode;
  }

  public void addFile(VirtualFile file) {
    final CheckedTreeNode root = (CheckedTreeNode)getModel().getRoot();
    final CheckedTreeNode treeNode = new CheckedTreeNode(file);
    root.add(treeNode);
    DefaultTreeModel model = (DefaultTreeModel)getModel();
    model.nodeStructureChanged(root);
  }
}
