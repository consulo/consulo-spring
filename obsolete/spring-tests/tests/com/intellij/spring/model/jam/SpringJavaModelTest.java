package com.intellij.spring.model.jam;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.javaConfig.SpringJavaBean;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.testFramework.PsiTestUtil;
import java.util.function.Consumer;

import java.util.List;

public class SpringJavaModelTest extends IdeaTestCase {
  private VirtualFile myRoot;
  private SpringJamModel myModel;

  public static void addSpringLibraryToRoots(final Module module) {
    PsiTestUtil.addLibrary(module, "Spring", PathManager.getHomePath() + "/svnPlugins/spring/spring-tests/testData/", "spring2.jar");
    PsiTestUtil.addLibrary(module, "SpringJavaConfig", PathManager.getHomePath() + "/svnPlugins/spring/spring-tests/testData/", "spring-javaconfig-annotations.jar");
  }


  protected void setUp() throws Exception {
    super.setUp();
    myRoot = getVirtualFile(createTempDirectory());
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        PsiTestUtil.addSourceRoot(getModule(), myRoot);
        addSpringLibraryToRoots(getModule());
      }
    }.execute();
    myModel = SpringJamModel.getModel(getModule());
  }

  public void testGetConfigurations() throws Throwable {
    final PsiJavaFile psiFile = createFile( "@" + SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION + "\nclass FooJavaConfig {}\n" +
                                            "@" + SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION + "\nclass FooJavaConfig2 {}\n" );
    final PsiClass fooJavaConfig = psiFile.getClasses()[0];
    final PsiClass fooJavaConfig2  = psiFile.getClasses()[1];

    assertUnorderedCollection(myModel.getConfigurations(), new Consumer<SpringJavaConfiguration>() {
      public void consume(final SpringJavaConfiguration configuration) {
        assertEquals(fooJavaConfig, configuration.getPsiClass());
      }
    }, new Consumer<SpringJavaConfiguration>() {
      public void consume(final SpringJavaConfiguration configuration) {
        assertEquals(fooJavaConfig2, configuration.getPsiClass());
      }
    });
  }

  public void testGetJavaSpringBeans() throws Throwable {
    final PsiJavaFile psiFile = createFile( "@" + SpringAnnotationsConstants
        .JAVA_CONFIG_CONFIGURATION_ANNOTATION + "\nclass FooJavaConfig {" +
                                            "@" + SpringAnnotationsConstants.JAVA_CONFIG_BEAN_ANNOTATION + "(aliases = {\"alias_1\", \"alias_2\"})\n java.lang.String fooBean() {} \n" +
                                            " void notBean() {} \n" +
                                            "@" + SpringAnnotationsConstants.JAVA_CONFIG_BEAN_ANNOTATION + "\n java.lang.Integer fooBean2() {} }\n");


    final PsiClass fooJavaConfig = psiFile.getClasses()[0];
    final List<? extends SpringJavaBean> javaBeans = myModel.getConfigurations().get(0).getBeans();

   assertUnorderedCollection(javaBeans, new Consumer<SpringJavaBean>() {
      public void consume(final SpringJavaBean javaBean) {
        assertEquals(fooJavaConfig.findMethodsByName("fooBean", false)[0],  javaBean.getPsiElement());
        assertEquals(javaBean.getBeanName(), "fooBean");
        assertEquals(javaBean.getBeanClass().getQualifiedName(), "java.lang.String");
        assertEquals(javaBean.getAliases().length, 2);
      }
    }, new Consumer<SpringJavaBean>() {
      public void consume(final SpringJavaBean javaBean) {
        assertEquals(fooJavaConfig.findMethodsByName("fooBean2", false)[0], javaBean.getPsiElement());
        assertEquals(javaBean.getBeanName(), "fooBean2");
        assertEquals(javaBean.getBeanClass().getQualifiedName(), "java.lang.Integer");
      }
    });
  }

  private PsiJavaFile createFile(final String text) {
    final VirtualFile file = new WriteCommandAction<VirtualFile>(getProject()) {
      protected void run(Result<VirtualFile> result) throws Throwable {
        final VirtualFile file = myRoot.createChildData(this, "FooJavaConfig.java");

        VfsUtil.saveText(file, text);
        result.setResult(file);
      }
    }.execute().getResultObject();


    return (PsiJavaFile)getPsiManager().findFile(file);
  }
 }
