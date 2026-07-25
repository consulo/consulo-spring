package com.intellij.spring.impl.ide.model.actions.generate;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nullable;

import consulo.codeEditor.Editor;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomElementNavigationProvider;

public class SpringAliasGenerateProvider extends BasicSpringDomGenerateProvider<Alias> {
  public SpringAliasGenerateProvider() {
    super(LocalizeValue.of(getDescription(Alias.class)), Alias.class);
  }

  @Override
  public Alias generate(@Nullable DomElement parent, Editor editor) {
    Alias alias = super.generate(parent, editor);

    if (alias != null) {
      alias.getAlias().ensureXmlElementExists();
      alias.getAliasedBean().ensureXmlElementExists();
    }

    return alias;
  }

  @Override
  protected DomElement getElementToNavigate(Alias alias) {
    return alias.getAliasedBean();
  }

  @Override
  protected void doNavigate(DomElementNavigationProvider navigateProvider, DomElement element) {
    navigateProvider.navigate(((Alias)element).getAliasedBean(), true);
  }
}
