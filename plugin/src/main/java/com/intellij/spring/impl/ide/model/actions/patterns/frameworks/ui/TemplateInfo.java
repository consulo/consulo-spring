package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;

import consulo.language.editor.template.Template;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util.StandardBeansDocLinksManager;

public class TemplateInfo {
  private Template myTemplate;
  private LocalizeValue myName;
  private String myReferenceLink;
  private String myApiLink;
  private LocalizeValue myDescription;
  private boolean myAccepted;

  public TemplateInfo(Module module, Template template, LocalizeValue name) {
     this(module, template, name, LocalizeValue.empty());
  }

  public TemplateInfo(Module module, Template template, LocalizeValue name, LocalizeValue description) {
     this(module, template, name, description, true);
  }

  public TemplateInfo(Module module, Template template, LocalizeValue name, LocalizeValue description, boolean isAccepted) {
    myTemplate = template;
    myName = name;
    StandardBeansDocLinksManager linksManager = StandardBeansDocLinksManager.getInstance(module.getProject());
    myReferenceLink = linksManager.getReferenceLink(template.getId());
    myApiLink = linksManager.getApiLink(template.getId());
    myDescription = description;
    myAccepted = isAccepted;
  }

  public Template getTemplate() {
    return myTemplate;
  }

  public boolean isAccepted() {
    return myAccepted;
  }

  public void setAccepted(boolean accepted) {
    myAccepted = accepted;
  }

  public String getName() {
    return myName.get();
  }

  public String getDescription() {
    return myDescription.get();
  }

  public String getReferenceLink() {
    return myReferenceLink;
  }

  public void setReferenceLink(String referenceLink) {
    myReferenceLink = referenceLink;
  }

  public String getApiLink() {
    return myApiLink;
  }

  public void setApiLink(String apiLink) {
    myApiLink = apiLink;
  }
}
