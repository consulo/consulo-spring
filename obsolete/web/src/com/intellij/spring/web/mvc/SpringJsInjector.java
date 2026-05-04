/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.spring.web.mvc;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSLanguageInjector;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.web.SpringWebConstants;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringJsInjector implements MultiHostInjector {

  private static final NamespaceFilter NAMESPACE_FILTER = new NamespaceFilter(SpringWebConstants.MVC_FORM_TLD);

  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement host) {
    final FileType fileType = host.getContainingFile().getFileType();
    if (fileType != StdFileTypes.JSP &&
        fileType != StdFileTypes.JSPX) {
      return;
    }

    if (host instanceof XmlAttributeValue) {
      final PsiElement tag = host.getParent().getParent();
      if (NAMESPACE_FILTER.isAcceptable(tag, null)) {
        @NonNls final String name = ((XmlAttribute) host.getParent()).getName();
        if (name.startsWith("on")) {
          JSLanguageInjector.injectJSIntoAttributeValue(registrar, (XmlAttributeValue)host, false);
        }
      }
    }
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }
}
