/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.java.language.psi.PsiParameter;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.content.scope.SearchScope;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.module.Module;
import consulo.xml.language.psi.XmlFile;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringAopParameterReferencesSearcher implements ReferencesSearchQueryExecutor {
    @Override
    public boolean execute(ReferencesSearch.SearchParameters queryParameters, Predicate<? super PsiReference> consumer) {
        //noinspection AutoUnboxing
        return ApplicationManager.getApplication().runReadAction((Supplier<Boolean>) () -> {
            SearchScope scope = queryParameters.getScope();
            if (scope instanceof MyLocalSearchScope) {
                return true; //recursive call
            }

            PsiElement element = queryParameters.getElementToSearch();
            if (element instanceof PsiParameter) {
                Module module = element.getModule();
                if (module != null) {
                    Set<XmlFile> visited = new HashSet<>();
                    for (SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
                        for (XmlFile xmlFile : model.getConfigFiles()) {
                            if (!visited.contains(xmlFile)) {
                                visited.add(xmlFile);
                                LocalSearchScope localScope = (LocalSearchScope) new LocalSearchScope(xmlFile).intersectWith(scope);
                                if (localScope.getScope().length > 0) {
                                    if (!ReferencesSearch.search(element, new MyLocalSearchScope(localScope), true).forEach(consumer)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            return true;
        });
    }

    private static class MyLocalSearchScope extends LocalSearchScope {
        public MyLocalSearchScope(LocalSearchScope scope) {
            super(scope.getScope(), scope.getDisplayName(), scope.isIgnoreInjectedPsi());
        }
    }
}
