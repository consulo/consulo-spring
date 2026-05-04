package com.intellij.spring.web.mvc;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.Pair;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.PsiElementPointer;
import com.intellij.javaee.web.facet.WebFacet;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public interface MVCViewProvider {

  ExtensionPointName<MVCViewProvider> EP_NAME = ExtensionPointName.create("com.intellij.spring.mvcViewProvider");

  List<Pair<String, PsiElementPointer>> getViews(SpringModel model, WebFacet facet);
}
