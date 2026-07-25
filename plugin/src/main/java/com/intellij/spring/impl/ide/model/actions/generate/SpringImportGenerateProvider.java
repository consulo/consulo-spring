package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.model.xml.beans.SpringImport;
import consulo.codeEditor.Editor;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomElement;
import jakarta.annotation.Nullable;

public class SpringImportGenerateProvider extends BasicSpringDomGenerateProvider<SpringImport> {
  public SpringImportGenerateProvider() {
    super(SpringLocalize.springImport(), SpringImport.class);
  }

  @Override
  public SpringImport generate(@Nullable DomElement parent, Editor editor) {
    SpringImport springImport = super.generate(parent, editor);

    if (springImport != null) {
      springImport.getResource().ensureXmlElementExists();
    }

    return springImport;
  }

  @Override
  protected DomElement getElementToNavigate(SpringImport springImport) {
    return springImport.getResource();
  }
}
