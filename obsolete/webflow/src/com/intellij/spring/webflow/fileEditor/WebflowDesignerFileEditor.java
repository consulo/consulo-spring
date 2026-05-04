package com.intellij.spring.webflow.fileEditor;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.project.Project;
import consulo.disposer.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class WebflowDesignerFileEditor extends PerspectiveFileEditor {

  private WebflowDesignerComponent myComponent;
  private final XmlFile myXmlFile;

  public WebflowDesignerFileEditor(final Project project, final VirtualFile file) {
    super(project, file);

    final PsiFile psiFile = getPsiFile();
    assert psiFile instanceof XmlFile;

    myXmlFile = (XmlFile)psiFile;
  }


  @Nullable
  protected DomElement getSelectedDomElement() {
    final List<DomElement> selectedDomElements = getWebflowDesignerComponent().getSelectedDomElements();

    return selectedDomElements.size() > 0 ? selectedDomElements.get(0) : null;
  }

  protected void setSelectedDomElement(final DomElement domElement) {
      getWebflowDesignerComponent().setSelectedDomElement(domElement);
  }

  @NotNull
  protected JComponent createCustomComponent() {
    return getWebflowDesignerComponent();
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return ((Graph2DView)getWebflowDesignerComponent().getBuilder().getGraph().getCurrentView()).getJComponent();
  }

  public void commit() {
  }

  public void reset() {
    getWebflowDesignerComponent().getBuilder().queueUpdate();
  }

  @NotNull
  public String getName() {
    return WebflowBundle.message("spring.webflow.designer");
  }

  public StructureViewBuilder getStructureViewBuilder() {
    return GraphViewUtil.createStructureViewBuilder(getWebflowDesignerComponent().getOverview());
  }

  private WebflowDesignerComponent getWebflowDesignerComponent() {
    if (myComponent == null) {
      myComponent = new WebflowDesignerComponent(myXmlFile);
      Disposer.register(this, myComponent);
    }
    return myComponent;
  }
}
