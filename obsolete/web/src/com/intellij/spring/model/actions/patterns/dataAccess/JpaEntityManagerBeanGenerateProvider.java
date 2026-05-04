/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.actions.patterns.dataAccess;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.*;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.MacroFactory;
import com.intellij.javaee.model.xml.persistence.PersistenceUnit;
import com.intellij.jpa.facet.JpaFacet;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.actions.generate.DomTemplateRunner;
import com.intellij.util.xml.impl.DomTemplateRunnerImpl;

import java.util.LinkedHashSet;

public class JpaEntityManagerBeanGenerateProvider extends SpringBeanGenerateProvider {
  public JpaEntityManagerBeanGenerateProvider() {
    super(SpringBundle.message("spring.patterns.data.access.jpa.entity.manager.factory"), null);
  }

  protected void runTemplate(final Editor editor, final PsiFile file, final SpringBean springBean) {
    super.runTemplate(editor, file, springBean);
    ((DomTemplateRunnerImpl)DomTemplateRunner.getInstance(file.getProject())).runTemplate(springBean,editor, getTemplate(springBean));
  }

  protected Template getTemplate(final SpringBean springBean) {
    return getTemplate(springBean.getManager().getProject());
  }

  public static Template getTemplate(Project project) {
    final TemplateManager manager = TemplateManager.getInstance(project);
    final Template template = manager.createTemplate("", "");
    template.setToReformat(true);

    Expression expression = new MacroCallNode(MacroFactory.createMacro("complete"));
    Expression persistenceUnitExpression = getPersistenceUnitExpression();

    //<bean id="$BEAN_NAME$" class="org.springframework.orm.jpa.LocalEntityManagerFactoryBean">
    //   <property name="persistenceUnitName" value="$PERSISTENCE_UNIT$"/>
    //</bean>
    template.addTextSegment("<bean id=\"");
    template.addVariable("BEAN_NAME", expression, expression, true);

    template.addTextSegment(
      "\" class=\"org.springframework.orm.jpa.LocalEntityManagerFactoryBean\">\n <property name=\"persistenceUnitName\" value=\"");
    template.addVariable("PERSISTENCE_UNIT", persistenceUnitExpression, persistenceUnitExpression, true);
    template.addTextSegment("\"/>\n</bean>");

    return template;
  }

  private static Expression getPersistenceUnitExpression() {
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        return new TextResult("");
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        final Project project = context.getProject();
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(context.getEditor().getDocument());
        if(psiFile == null) return LookupElement.EMPTY_ARRAY;
        final Module module = ModuleUtil.findModuleForPsiElement(psiFile);

        if(module == null) return LookupElement.EMPTY_ARRAY;

        final JpaFacet jpaFacet = JpaFacet.getInstance(module);

        if(jpaFacet == null) return LookupElement.EMPTY_ARRAY;

        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();

        for (PersistenceUnit persistenceUnit : jpaFacet.getPersistenceUnits()) {
          final String stringValue = persistenceUnit.getName().getStringValue();
          if (StringUtil.isNotEmpty(stringValue)) {
            items.add(LookupElementBuilder.create(stringValue).setIcon(ElementPresentationManager.getIcon(persistenceUnit)));
          }
        }

        return items.toArray(new LookupElement[items.size()]);
      }
    };
  }
}
