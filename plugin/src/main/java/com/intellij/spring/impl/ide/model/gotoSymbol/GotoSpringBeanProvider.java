package com.intellij.spring.impl.ide.model.gotoSymbol;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.navigation.NavigationItem;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.lang.StringUtil;
import consulo.xml.util.xml.model.gotosymbol.GoToSymbolProvider;

import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sergey.Vasiliev
 */
@ExtensionImpl
public class GotoSpringBeanProvider extends GoToSymbolProvider {
  @Override
  @RequiredReadAction
  protected void addNames(@Nonnull Module module, Set<String> result) {
    SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    if(springModel != null) {
      for (SpringBaseBeanPointer pointer : springModel.getAllCommonBeans()) {
        result.addAll(getNames(pointer));
      }
    }
  }

  private void addNonNull(Set<String> result, String alias) {
    if (!StringUtil.isEmptyOrSpaces(alias)) {
      result.add(alias);
    }
  }

  @Override
  @RequiredReadAction
  protected void addItems(@Nonnull Module module, String name, List<NavigationItem> result) {
    SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    if(springModel != null) {
      for (SpringBaseBeanPointer pointer : springModel.getAllCommonBeans()) {
        Set<String> beanNames = getNames(pointer);
        if (beanNames.contains(name)) {
          PsiElement element = pointer.getSpringBean().getIdentifyingPsiElement();

          if (element != null) {
            result.add(createNavigationItem(element, name, pointer.getBeanIcon()));
          }
        }
      }
    }
  }

  private Set<String> getNames(SpringBaseBeanPointer pointer) {
    Set<String> names = new HashSet<>();
    addNonNull(names, pointer.getName());

    for (String alias : pointer.getAliases()) {
      addNonNull(names, alias);
    }
    return names;
  }

  @Override
  @RequiredReadAction
  protected boolean acceptModule(Module module) {
    return SpringModuleExtension.getInstance(module) != null;
  }
}

