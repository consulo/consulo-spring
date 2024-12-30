/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiFormatUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.ResolvedConstructorArgs;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.actions.generate.SpringTemplateBuilder;
import com.intellij.spring.impl.ide.model.converters.SpringBeanUtil;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.DomUtil;
import consulo.xml.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtensionImpl
public class SpringConstructorArgInspection extends SpringBeanInspectionBase {

  @Nonnull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.bean.constructor.arg");
  }

  @Nonnull
  @NonNls
  public String getShortName() {
    return "SpringBeanConstructorArgInspection";
  }


  protected void checkBean(SpringBean springBean,
                           final Beans beans,
                           final DomElementAnnotationHolder holder,
                           final SpringModel springModel,
                           Object state) {
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      checkConstructorResolve(springBean, holder, beanClass);
      checkConstructorArgType(springBean, holder);
    }
    checkConstructorArgIndexes(springBean, holder);
  }

  // checks if instantiation method matches the args
  private static void checkConstructorResolve(@Nonnull SpringBean springBean, final DomElementAnnotationHolder holder, @Nonnull final PsiClass beanClass) {
    if(springBean.isAbstract()) return;
    
    final ResolvedConstructorArgs resolvedArgs = springBean.getResolvedConstructorArgs();

    if (!resolvedArgs.isResolved()) {
      final boolean instantiatedByFactory = isInstantiatedByFactory(springBean);
      final String message = instantiatedByFactory ? SpringBundle.message("cannot.find.factory.method.with.parameters.count", beanClass.getName()) :
                SpringBundle.message("cannot.find.bean.constructor.with.parameters.count", beanClass.getName());

      final DomElement element;
      if (!instantiatedByFactory && DomUtil.hasXml(springBean.getClazz())) {
        element = springBean.getClazz();
      } else if (instantiatedByFactory && DomUtil.hasXml(springBean.getFactoryMethod())) {
        element = springBean.getFactoryMethod();
      } else {
        element = springBean;
      }

      final List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();

      final SpringBean stableCopy = springBean.createStableCopy();
      if (!instantiatedByFactory && !(beanClass instanceof PsiCompiledElement)) {
        fixes.add(createConstructorQuickFix(stableCopy, beanClass));
      }
      fixes.addAll(getConstructroArgsQuickFixes(stableCopy, beanClass.getConstructors()));

      holder.createProblem(element, HighlightSeverity.ERROR, message, fixes.toArray(new LocalQuickFix[fixes.size()]));
    }
  }

  private static boolean isInstantiatedByFactory(final SpringBean springBean) {
    return springBean.getFactoryMethod().getXmlAttribute() != null;
  }

  private static void checkConstructorArgType(final SpringBean springBean, final DomElementAnnotationHolder holder) {
    final List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);
    if (list.size() == 0) {
      return;
    }
    final List<PsiMethod> instantiationMethods = SpringBeanUtil.getInstantiationMethods(springBean);
    args: for (final ConstructorArg arg : list) {
      final PsiType argType = arg.getType().getValue();
      if (argType == null) {
        continue;
      }
      final Integer index = arg.getIndex().getValue();
      boolean parameterFound = false;
      if (index != null) {
        final int i = index.intValue();
        if (i < 0) {
          continue;
        }
        for (final PsiMethod method : instantiationMethods) {
          final PsiParameter[] parameters = method.getParameterList().getParameters();
          if (i < parameters.length) {
            parameterFound = true;
            if (parameters[i].getType().isAssignableFrom(argType)) {
              continue args;
            }
          }
        }
      } else {
        for (final PsiMethod method : instantiationMethods) {
          final PsiParameter[] parameters = method.getParameterList().getParameters();
          for (PsiParameter param: parameters) {
            if (param.getType().isAssignableFrom(argType)) {
              continue args;
            }
          }
        }
      }
      if (parameterFound) {
        final String message = SpringBundle.message("constructor.arg.incorrect.value.type");
        holder.createProblem(arg.getType(), message);
      }
    }
  }

  private static void checkConstructorArgIndexes(final SpringBean springBean, final DomElementAnnotationHolder holder) {
    final List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);

    Map<Integer, ConstructorArg> argsMap = new HashMap<Integer, ConstructorArg>();
    final ArrayList<ConstructorArg> firstSeen = new ArrayList<ConstructorArg>();
    for (ConstructorArg arg : list) {
      final Integer index = arg.getIndex().getValue();
      if (index != null) {
        final ConstructorArg previous = argsMap.put(index, arg);
        if (previous != null) {
          reportNotUniqueIndex(holder, arg);
          if (!firstSeen.contains(previous)) {
            reportNotUniqueIndex(holder, previous);
            firstSeen.add(previous);
          }
        }
      }
    }
  }

  private static void reportNotUniqueIndex(final DomElementAnnotationHolder holder, final ConstructorArg arg) {
    final String message = SpringBundle.message("incorrect.constructor.arg.index.not.unique");
    holder.createProblem(arg.getIndex(), message);
  }

  private static LocalQuickFix createConstructorQuickFix(final SpringBean springBean, final PsiClass beanClass) {
    return new LocalQuickFix() {

      @Nonnull
      public String getName() {
        return SpringBundle.message("model.create.constructor.quickfix.message", getSignature(springBean));
      }

      private String getSignature(final SpringBean springBean) {
        final String params = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgsAsString(springBean);
        return beanClass.getName() + "(" + params + ")";
      }

      @Nonnull
      public String getFamilyName() {
        return SpringBundle.message("model.bean.quickfix.family");
      }

      public void applyFix(@Nonnull final Project project, @Nonnull final ProblemDescriptor descriptor) {

        final PsiClass beanClass = springBean.getBeanClass();
        try {
          assert beanClass != null;
          if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(beanClass.getContainingFile().getVirtualFile()).hasReadonlyFiles()) {
            return;
          }
          final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

          final PsiMethod constructor = elementFactory.createConstructor();
          final List<PsiParameter> parameters = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean);
          for (PsiParameter parameter : parameters) {
            constructor.getParameterList().add(parameter);
          }

          beanClass.add(constructor);
        }
        catch (IncorrectOperationException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static List<LocalQuickFix> getConstructroArgsQuickFixes(final @Nonnull SpringBean springBean, @Nonnull PsiMethod[] ctors) {
    List<LocalQuickFix> quickFixes = new ArrayList<LocalQuickFix>();

    if (SpringUtils.getConstructorArgs(springBean).size() == 0) {
      for (PsiMethod ctor : ctors) {
        if (ctor.getParameterList().getParametersCount() > 0) {
          quickFixes.add(new AddConstructorArgQuickFix(ctor, springBean));
        }
      }
    }

    return quickFixes;
  }

  public static class AddConstructorArgQuickFix implements LocalQuickFix
  {

    private final SpringBean mySpringBean;
    private final String myMethodName;
    private final SmartPsiElementPointer<PsiMethod> myPointer;

    public AddConstructorArgQuickFix(final PsiMethod ctor, final SpringBean springBean) {
      myPointer = SmartPointerManager.getInstance(ctor.getProject()).createSmartPsiElementPointer(ctor);
      mySpringBean = springBean;
      myMethodName = PsiFormatUtil
        .formatMethod(ctor, PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS, PsiFormatUtil.SHOW_TYPE);
    }

    @Nonnull
    public String getName() {
      return SpringBundle.message("model.add.constructor.args.for.method.quickfix.message", myMethodName);
    }

    @Nonnull
    public String getFamilyName() {
      return SpringBundle.message("model.bean.quickfix.family");
    }

    public void applyFix(@Nonnull final Project project, @Nonnull final ProblemDescriptor descriptor) {

      PsiMethod myCtor = myPointer.getElement();
      if (myCtor == null) {
        return;
      }
      @SuppressWarnings({"ConstantConditions"})
      PsiMethod[] myAllCtors = myCtor.getContainingClass().getConstructors();
      final PsiParameter[] parameters = myCtor.getParameterList().getParameters();
      final SpringModel model = SpringUtils.getSpringModel(mySpringBean);
      final SpringTemplateBuilder builder = new SpringTemplateBuilder(project);
      final Editor editor = SpringTemplateBuilder.getEditor(descriptor);
      SpringTemplateBuilder.preparePlace(editor, project, mySpringBean.addConstructorArg());

      for (int i = 0; i < parameters.length; i++) {
        PsiParameter parameter = parameters[i];
        builder.addTextSegment("<constructor-arg");

        if (parameters.length > 1) {
          builder.addTextSegment(" index=\"" + i + "\"");
        }
        for (PsiMethod ctor : myAllCtors) {
          if (ctor == myCtor) {
            continue;
          }
          final PsiParameter[] params = ctor.getParameterList().getParameters();
          if (params.length == parameters.length) {
            builder.addTextSegment(" type=\"" + parameters[i].getType().getCanonicalText() + "\"");
            break;
          }
        }
        builder.createValueAndClose(parameter.getType(), model, "constructor-arg");
      }
      builder.startTemplate(editor);
    }
  }
}