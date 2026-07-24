/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.codeInsight.completion.CompletionBundle;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionContributor;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.CompletionType;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiReference;
import consulo.ui.ex.action.IdeActions;
import consulo.xml.language.XMLLanguage;
import consulo.xml.language.psi.XmlToken;
import consulo.xml.dom.Converter;
import consulo.xml.util.xml.impl.GenericDomValueReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringCompletionContributor extends CompletionContributor {

  @Override
  public void fillCompletionVariants(final CompletionParameters parameters, final CompletionResultSet result) {

    if (parameters.getCompletionType() != CompletionType.SMART) {
      return;
    }
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        GenericDomValueReference reference = getReference(parameters);
        if (reference == null) {
          return;
        }

        Collection<SpringBeanPointer> variants =
          ((SpringBeanResolveConverter)reference.getConverter()).getSmartVariants(reference.getConvertContext());
        for (SpringBeanPointer variant : variants) {
          LookupElementBuilder element = SpringBeanResolveConverter.createCompletionVariant(variant);
          if (element != null) {
            result.addElement(element);
          }
        }
      }
    });
  }

  @Nullable
  private GenericDomValueReference getReference(CompletionParameters parameters) {

    if (!(parameters.getPosition() instanceof XmlToken)) return null;

    PsiReference[] references = parameters.getPosition().getParent().getReferences();
    for (PsiReference psiReference : references) {
      if (psiReference instanceof GenericDomValueReference) {
        Converter converter = ((GenericDomValueReference)psiReference).getConverter();
        if (converter instanceof SpringBeanResolveConverter) {
          return (GenericDomValueReference)psiReference;
        }
      }
    }
    return null;
  }

  @Override
  public String advertise(@Nonnull CompletionParameters parameters) {
    if (parameters.getCompletionType() == CompletionType.BASIC && getReference(parameters) != null) {
      String shortcut = getActionShortcut(IdeActions.ACTION_SMART_TYPE_COMPLETION);
      if (shortcut != null) {
        return CompletionBundle.message("completion.smart.hint", shortcut);
      }
    }
    return null;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }
}
