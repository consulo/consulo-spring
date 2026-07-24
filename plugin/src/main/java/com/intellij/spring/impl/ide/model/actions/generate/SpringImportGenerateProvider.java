package com.intellij.spring.impl.ide.model.actions.generate;

import consulo.codeEditor.Editor;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.xml.beans.SpringImport;
import consulo.xml.dom.DomElement;
import jakarta.annotation.Nullable;

public class SpringImportGenerateProvider extends BasicSpringDomGenerateProvider<SpringImport> {
  public SpringImportGenerateProvider() {
    super(SpringBundle.message("spring.import"), SpringImport.class);
  }

  public SpringImport generate(@Nullable DomElement parent, Editor editor) {
    SpringImport springImport = super.generate(parent, editor);

    if (springImport != null) {
      springImport.getResource().ensureXmlElementExists();
    }

    return springImport;
  }

  protected DomElement getElementToNavigate(SpringImport springImport) {
    return springImport.getResource();
  }
}
