package com.intellij.spring.impl.ide.model.structure;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.structureView.StructureView;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.project.Project;
import consulo.xml.ide.structureView.xml.XmlStructureViewBuilderProvider;
import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

//@ExtensionImpl disable - due need write it, without ref to FilEditor - need remove UnsupportedOperationException throwing
public class SpringXmlStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {
  @Nullable
  public StructureViewBuilder createStructureViewBuilder(@Nonnull final XmlFile file) {
    SpringManager springManager = SpringManager.getInstance(file.getProject());
    if (springManager.isSpringBeans(file)) {
      SpringModel localSpringModel = springManager.getLocalSpringModel(file);
      if (localSpringModel != null) {
        return new TreeBasedStructureViewBuilder() {
          @Nonnull
          public StructureViewModel createStructureViewModel() {
            return new SpringStructureViewModel(file) {
              @Nonnull
              public Sorter[] getSorters() {
                SpringManager springManager = SpringManager.getInstance(file.getProject());
                return springManager.isSpringBeans(file) ? new Sorter[]{Sorter.ALPHA_SORTER} : Sorter.EMPTY_ARRAY;
              }
            };
          }

          public boolean isRootNodeShown() {
            return true;
          }

          @Nonnull
          @Override
          public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
            throw new UnsupportedOperationException();
          }

          @Nonnull
          public StructureView createStructureView(FileEditor fileEditor, Project project) {
            return new SpringStructureViewComponent(fileEditor, createStructureViewModel(), project);

          }
        };
      }
    }
    return null;
  }


}
