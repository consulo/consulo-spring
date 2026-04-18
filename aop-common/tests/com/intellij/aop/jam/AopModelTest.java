/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.*;
import com.intellij.aop.psi.AllAdvisedElementsSearcher;
import com.intellij.testFramework.IdeaTestUtil;
import consulo.application.util.function.Processor;
import consulo.language.editor.WriteCommandAction;
import consulo.component.extension.Extensions;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import java.util.function.Consumer;

import consulo.application.Result;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author peter
 */
public class AopModelTest extends JavaCodeInsightFixtureTestCase {
  private VirtualFile myRoot;
  private AopModelImpl myModel;

  protected void setUp() throws Exception {
    super.setUp();
    myRoot = myFixture.getTempDirFixture().getFile("");
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        PsiTestUtil.addSourceRoot(myModule, myRoot);
      }
    }.execute();
    AopLiteFixture.addAopAnnotations(myFixture);
    myModel = AopModuleService.getService(myModule).getModel();
  }

  public void testGetAspects() throws Throwable {
    final PsiJavaFile psiFile = createFile( "@" + AopConstants.ASPECT_ANNO + "\nclass FooAspect {}\n" +
                                            "@" + AopConstants.ASPECT_ANNO + "\nclass BarAspect {}");
    final PsiClass fooAspectClass = psiFile.getClasses()[0];
    final PsiClass barAspectClass = psiFile.getClasses()[1];

    assertUnorderedCollection(myModel.getAspects(), new Consumer<AopAspect>() {
      public void consume(final AopAspect aopAspect) {
        assertEquals(fooAspectClass, aopAspect.getPsiClass());
      }
    }, new Consumer<AopAspect>() {
      public void consume(final AopAspect aopAspect) {
        assertEquals(barAspectClass, aopAspect.getPsiClass());
      }
    });
  }

  public void testGetPointcuts() throws Throwable {
    final PsiJavaFile psiFile = createFile( "@" + AopConstants.ASPECT_ANNO + "\nclass FooAspect {" +
                                            "@" + AopConstants.POINTCUT_ANNO + " void foo() {} }\n" +

                                            "@" + AopConstants.ASPECT_ANNO + "\nclass BarAspect {" +
                                            "@" + AopConstants.POINTCUT_ANNO + " void bar() {} } " +

                                            "class NotAspect {" +
                                            "@" + AopConstants.POINTCUT_ANNO + " void foo() {} }");

    final PsiClass fooAspectClass = psiFile.getClasses()[0];
    final PsiClass barAspectClass = psiFile.getClasses()[1];
    final PsiClass notAspectClass = psiFile.getClasses()[2];

    final List<AopPointcutImpl> list = myModel.getPointcuts();
    assertUnorderedCollection(list, new Consumer<AopPointcut>() {
      public void consume(final AopPointcut aopPointcut) {
        assertEquals(fooAspectClass.getMethods()[0], ((AopPointcutImpl) aopPointcut).getPsiElement());
        assertEquals("FooAspect.foo", aopPointcut.getQualifiedName().getValue());
      }
    }, new Consumer<AopPointcut>() {
      public void consume(final AopPointcut aopPointcut) {
        assertEquals(barAspectClass.getMethods()[0], ((AopPointcutImpl) aopPointcut).getPsiElement());
        assertEquals("BarAspect.bar", aopPointcut.getQualifiedName().getValue());
      }
    }, new Consumer<AopPointcut>() {
      public void consume(final AopPointcut aopPointcut) {
        assertEquals(notAspectClass.getMethods()[0], ((AopPointcutImpl) aopPointcut).getPsiElement());
        assertEquals("NotAspect.foo", aopPointcut.getQualifiedName().getValue());
      }
    });
  }

  public void testGetAdvices() throws Throwable {
    final PsiJavaFile psiFile = createFile( "@" + AopConstants.ASPECT_ANNO + "\nclass FooAspect {" +
                                            "@" + AopConstants.BEFORE_ANNO + " void before() {} \n" +
                                            "@" + AopConstants.AFTER_ANNO + " void after() {} \n" +
                                            "@" + AopConstants.AFTER_RETURNING_ANNO + " void afterReturning() {} \n" +
                                            "@" + AopConstants.AFTER_THROWING_ANNO + " void afterThrowing() {} \n" +
                                            "@" + AopConstants.AROUND_ANNO + "(\"execution\") void around() {} \n" +
                                            "}");

    final PsiClass fooAspectClass = psiFile.getClasses()[0];
    final List<? extends AopAdvice> advices = myModel.getAspects().get(0).getAdvices();

    Consumer<AopAdvice>[] checkers = new Consumer[5];
    for (int i = 0; i < checkers.length; i++) {
      final int i1 = i;
      checkers[i] = new Consumer<AopAdvice>() {
        public void consume(final AopAdvice aopAdvice) {
          assertEquals(fooAspectClass.getMethods()[i1], ((AopAdviceImpl) aopAdvice).getPsiElement());
          final AopAdviceType adviceType = aopAdvice.getAdviceType();
          assertEquals(AopAdviceType.values()[i1], adviceType);
          if (adviceType == AopAdviceType.AFTER_RETURNING) assertInstanceOf(aopAdvice, AopAfterReturningAdviceImpl.class);
          else if (adviceType == AopAdviceType.AFTER_THROWING) assertInstanceOf(aopAdvice, AopAfterThrowingAdviceImpl.class);
        }
      };
    }

    assertUnorderedCollection(advices, checkers);
  }

  public void testGetIntroductions() throws Throwable {
    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new AopProvider() {
      @Nullable
      public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull final PsiClass aClass) {
        return new AllAdvisedElementsSearcher(getPsiManager());
      }
    }, getTestRootDisposable());

    final PsiJavaFile psiFile = createFile("@" + AopConstants.ASPECT_ANNO + "\nclass FooAspect {" +
                                           "@" + AopConstants.DECLARE_PARENTS_ANNO + "(value=\"aaa.bbb.*+\",defaultImpl=\"java.lang.String\") Object before; \n" +
                                           "}");

    final PsiClass fooAspectClass = psiFile.getClasses()[0];
    final AopAspectImpl aspect = myModel.getAspects().get(0);
    assertUnorderedCollection(aspect.getIntroductions(), new Consumer<AopIntroduction>() {
      public void consume(final AopIntroduction aopIntroduction) {
        assertEquals(fooAspectClass.getFields()[0], ((AopIntroductionImpl)aopIntroduction).getPsiElement());
        assertEquals("aaa.bbb.*+", aopIntroduction.getTypesMatching().getStringValue());
        assertEquals("aaa.bbb.*+", aopIntroduction.getTypesMatching().getValue().getText());
        assertEquals(CommonClassNames.JAVA_LANG_STRING, aopIntroduction.getDefaultImpl().getStringValue());
        assertEquals(getJavaFacade().findClass(CommonClassNames.JAVA_LANG_STRING), aopIntroduction.getDefaultImpl().getValue());
        assertEquals(CommonClassNames.JAVA_LANG_OBJECT, aopIntroduction.getImplementInterface().getStringValue());
        assertEquals(getJavaFacade().findClass(CommonClassNames.JAVA_LANG_OBJECT), aopIntroduction.getImplementInterface().getValue());
      }
    });
  }

  private JavaPsiFacade getJavaFacade() {
    return JavaPsiFacade.getInstance(getProject());
  }

  private PsiJavaFile createFile(final String text) {
    final VirtualFile file = new WriteCommandAction<VirtualFile>(getProject()) {
      protected void run(Result<VirtualFile> result) throws Throwable {
        final VirtualFile file = myRoot.createChildData(this, "Aspects.java");

        VirtualFileUtil.saveText(file, text);
        result.setResult(file);
      }
    }.execute().getResultObject();


    return (PsiJavaFile)getPsiManager().findFile(file);
  }

  public void testPointcutExpression() throws Throwable {
    final PsiMethod method = getJavaFacade().getElementFactory()
      .createMethodFromText("@" + AopConstants.POINTCUT_ANNO + "(\"xxx\")\n" + "void foo(int a) {}", null);
    final AopPointcut pointcut = new AopPointcutImpl() {
      @Nonnull
      public PsiMethod getPsiElement() {
        return method;
      }
    };
    assertEquals("xxx", pointcut.getExpression().getStringValue());
  }

  public void testPointcutExpressionInPointcutAttribute() throws Throwable {
    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new AopProvider() {

      public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull final PsiClass aClass) {
        return new AopAdvisedElementsSearcher(aClass.getManager()) {
          public boolean process(final Processor<PsiClass> processor) {
            throw new UnsupportedOperationException("Method doProcess is not yet implemented in " + getClass().getName());
          }
        };
      }
    }, getTestRootDisposable());

    final PsiMethod method = myFixture.addClass("class Foo { " +
                                                "@" + AopConstants.AFTER_RETURNING_ANNO + "(pointcut=\"xxx\")\n" + "void foo(int a) {}" +
                                                "}").getMethods()[0];
    final AopAfterReturningAdviceImpl advice = new AopAfterReturningAdviceImpl() {
      public PsiMethod getPsiElement() {
        return method;
      }
    };
    assertEquals("xxx", advice.getPointcutExpression().getText());
  }

}
