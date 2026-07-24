package com.intellij.spring.impl.ide.model.structure;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.navigation.ItemPresentation;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.ui.image.Image;
import consulo.xml.ide.structureView.impl.xml.XmlTagTreeElement;
import consulo.xml.language.psi.XmlDocument;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomElementNavigationProvider;
import consulo.xml.dom.DomElementsNavigationManager;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SpringModelTreeElement implements StructureViewTreeElement, ItemPresentation {

  private XmlFile myXmlFile;
  private DomElementNavigationProvider myNavigationProvider;
  private boolean myShowBeanStructure;

  public SpringModelTreeElement(XmlFile xmlFile, boolean showBeanStructure) {
    this(xmlFile, DomElementsNavigationManager.getManager(xmlFile.getProject()).getDomElementsNavigateProvider(
      DomElementsNavigationManager.DEFAULT_PROVIDER_NAME), showBeanStructure);
  }

  public SpringModelTreeElement(XmlFile xmlFile, DomElementNavigationProvider navigationProvider, boolean showBeanStructure) {
    myXmlFile = xmlFile;
    myNavigationProvider = navigationProvider;
    myShowBeanStructure = showBeanStructure;
  }

  @Override
  public Object getValue() {
    return myXmlFile;
  }

  @Override
  public ItemPresentation getPresentation() {
    return this;
  }

  @Override
  public TreeElement[] getChildren() {
    List<StructureViewTreeElement> treeElements = new ArrayList<StructureViewTreeElement>();
    SpringModel springModel = getSpringModel();
    if (springModel != null) {
      for (SpringBaseBeanPointer pointer : springModel.getAllDomBeans()) {
        CommonSpringBean springBean = pointer.getSpringBean();
        if (pointer.isValid() && springBean.isValid() && springBean instanceof DomSpringBean) {
          treeElements
            .add(new SpringBeanTreeElement((DomSpringBean)springBean, myNavigationProvider, myShowBeanStructure));
        }
      }
    }
    DomManager manager = DomManager.getDomManager(myXmlFile.getProject());
    XmlDocument document = myXmlFile.getDocument();
    if (document != null) {
      XmlTag rootTag = document.getRootTag();
      if (rootTag != null) {
        for (XmlTag tag : rootTag.getSubTags()) {
          DomElement element = manager.getDomElement(tag);
          if (element instanceof CustomBeanWrapper && ((CustomBeanWrapper)element).isDummy()) {
            treeElements.add(new XmlTagTreeElement(tag));
          }
        }
      }
    }

    return treeElements.toArray(new TreeElement[treeElements.size()]);
  }

  @Nullable
  private SpringModel getSpringModel() {
    return SpringManager.getInstance(myXmlFile.getProject()).getLocalSpringModel(myXmlFile);
  }

  @Override
  public void navigate(boolean requestFocus) {
  }

  @Override
  public boolean canNavigate() {
    return false;
  }

  @Override
  public boolean canNavigateToSource() {
    return false;
  }

  @Override
  public String getPresentableText() {
    return SpringBundle.message("spring.beans");
  }

  @Override
  public String getLocationString() {
    return null;
  }

  @Override
  public Image getIcon() {
    return SpringImplIconGroup.springbean();
  }
}
