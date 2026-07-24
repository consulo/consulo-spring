package com.intellij.spring.impl.ide.model.actions.patterns.integration;

import com.intellij.java.impl.codeInsight.lookup.LookupItemUtil;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringPropertyDefinition;
import consulo.codeEditor.Editor;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupItem;
import consulo.language.editor.template.*;
import consulo.language.editor.template.macro.MacroCallNode;
import consulo.language.editor.template.macro.MacroFactory;
import consulo.language.psi.PsiFile;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.util.xml.actions.generate.DomTemplateRunner;
import consulo.xml.util.xml.impl.DomTemplateRunnerImpl;

import jakarta.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class MethodInvokingFactoryBean extends SpringBeanGenerateProvider {
  public MethodInvokingFactoryBean(String text) {
    super(text, null);
  }

  @Override
  protected void runTemplate(Editor editor, PsiFile file, SpringBean springBean, Map<String, String> predefinedVars) {
    super.runTemplate(editor, file, springBean, predefinedVars);
    ((DomTemplateRunnerImpl)DomTemplateRunner.getInstance(file.getProject())).runTemplate(springBean,editor, getTemplate(springBean));
  }

  protected Template getTemplate(SpringBean springBean) {
    TemplateManager manager = TemplateManager.getInstance(springBean.getManager().getProject());
    Template template = manager.createTemplate("", "");
    template.setToReformat(true);

    Expression completeExpression = new MacroCallNode(MacroFactory.createMacro("complete"));
    Expression targetMethodExpression = getTargetMethodExpression(springBean);

    //<bean id="$BEAN_NAME$" class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean">
    //  <property name="targetObject" ref="$TARGET_OBJECT$" />
    //  <property name="targetMethod" value="$TARGET_METHOD$" />
    //</bean>

    template.addTextSegment("<bean id=\"");
    template.addVariable("BEAN_NAME", completeExpression, completeExpression, true);

    template.addTextSegment("\" class=\""+getClassName()+"\">");
    template.addTextSegment("<property name=\"targetObject\" ref=\"");
    template.addVariable("TARGET_OBJECT", completeExpression, completeExpression, true);
    template.addTextSegment("\" />");
    template.addTextSegment("<property name=\"targetMethod\" value=\"");
    template.addVariable("TARGET_METHOD", targetMethodExpression, targetMethodExpression, true);
    template.addTextSegment("\" /></bean>");


    return template;
  }

  abstract protected String getClassName();

  private static Expression getTargetMethodExpression(SpringBean springBean) {
    final SpringBean copy = springBean.createStableCopy();
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        return new TextResult("");
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        PsiClass psiClass = getTargetObjectPsiClass(copy);
        if(psiClass == null) return LookupItem.EMPTY_ARRAY;

        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (PsiMethod psiMethod : psiClass.getAllMethods()) {
           if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC) && psiMethod.getParameterList().getParametersCount() == 0) {
             items.add(LookupItemUtil.objectToLookupItem(psiMethod));
           }
        }

        return items.toArray(new LookupElement[items.size()]);
      }
    };
  }

  @Nullable
  private static PsiClass getTargetObjectPsiClass(SpringBean springBean) {
    SpringPropertyDefinition property = SpringUtils.findPropertyByName(springBean, "targetObject");
    if (property != null) {
      GenericDomValue<SpringBeanPointer> element = property.getRefElement();
      if (element != null) {
        SpringBeanPointer beanPointer = element.getValue();
        if(beanPointer != null) {
          return beanPointer.getBeanClass();
        }
      }
    }
    return null;
  }
}
