package com.intellij.spring.model.xml.highlighting;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.converters.SpringBeanUtil;
import com.intellij.spring.model.highlighting.jam.SpringExternalBeanReferencesRenameHandler;
import com.intellij.spring.model.highlighting.jam.SpringExternalBeanRenameHandler;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;

public class JamAndXmlConfigurationTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addSpringJar(moduleBuilder);
    moduleBuilder.addLibraryJars("SpringJavaConfig", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 "spring-javaconfig-annotations.jar");
  }

  public void testJamExternalBeansHighlighting() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "jam_external_beans.xml");
    myFixture.testHighlighting(true, false, false, "JavaConfigExternalBeans.java", "FooBean.java");
  }

  public void testJamAndXmlConfiguredBeansHighlighting() throws Throwable {
    myFixture.configureByFiles( "jam-and-xml-configured-beans.xml",
                               "beans/JavaConfiguration.java",
                               "beans/FooBean7.java", "beans/FooBean4.java", "beans/FooBean.java", "beans/FooBean2.java", "beans/FooBean3.java");
    myFixture.allowTreeAccessForFile(getFile(myFixture.getTempDirPath() + "/beans/JavaConfiguration.java") );
    myFixture.testHighlighting(false, false, false, "jam-and-xml-configured-beans.xml",
                               "beans/JavaConfiguration.java",
                               "beans/FooBean7.java", "beans/FooBean4.java", "beans/FooBean.java", "beans/FooBean2.java", "beans/FooBean3.java");
  }

  public void testJamAndXmlConfiguredCompletion() throws Throwable {
    myFixture.testCompletion("jam-and-xml-configured-befor.xml",
                             "jam-and-xml-configured-after.xml",
                             "beans/JavaConfiguration.java",
                             "beans/FooBean7.java", "beans/FooBean.java", "beans/FooBean2.java", "beans/FooBean3.java");
  }

  public void testExternalBeanReferencesRename() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("jam_external_beans_rename.xml",
                                   "jam_external_beans_rename2.xml",
                                   "JavaConfigExternalBeanRename1.java",
                                   "JavaConfigExternalBeans2.java","FooBean.java", "FooBean3.java");
        fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename.xml"));
        fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename2.xml"));
        final DomSpringBean springBean = SpringBeanUtil.getTargetSpringBean();
        assertNotNull(springBean);
        SpringExternalBeanReferencesRenameHandler.doRename(springBean, "externalBean_new_name", false);
        myFixture.checkResultByFile("JavaConfigExternalBeanRename1.java", "JavaConfigExternalBeansRename1_after.java", true);
        myFixture.checkResultByFile("JavaConfigExternalBeans2.java", "JavaConfigExternalBeans2_after.java", true);
      }
    }.execute().throwException();
  }

  public void testExternalBeanReferencesRename_Diff_Filesets() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    final SpringFileSet fileSet2 = configureFileSet();

    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("jam_external_beans_rename.xml","jam_external_beans_rename2.xml","JavaConfigExternalBeanRename1.java","JavaConfigExternalBeans2.java");
        fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename.xml"));
        fileSet2.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename2.xml"));
        final DomSpringBean springBean = SpringBeanUtil.getTargetSpringBean();
        assertNotNull(springBean);

        SpringExternalBeanReferencesRenameHandler.doRename(springBean, "externalBean_new_name", false);
        myFixture.checkResultByFile("jam_external_beans_rename2_after.xml");
        myFixture.checkResultByFile("jam_external_beans_rename_after.xml");
        myFixture.checkResultByFile("JavaConfigExternalBeanRename1.java", "JavaConfigExternalBeansRename1_after.java", true);
        myFixture.checkResultByFile("JavaConfigExternalBeans2.java", "JavaConfigExternalBeans2_after.java", true);
      }
    }.execute().throwException();
  }

  public void testExternalBeanReferencesRename_Diff_Filesets2() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    final SpringFileSet fileSet2 = configureFileSet();

    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("jam_external_beans_rename.xml","jam_external_beans_rename_no_java_config.xml","JavaConfigExternalBeanRename1.java","JavaConfigExternalBeans2.java");
        fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename.xml"));
        fileSet2.addFile(myFixture.getTempDirFixture().getFile("jam_external_beans_rename_no_java_config.xml"));
        final DomSpringBean springBean = SpringBeanUtil.getTargetSpringBean();
        assertNotNull(springBean);

        SpringExternalBeanReferencesRenameHandler.doRename(springBean, "externalBean_new_name", false);
        myFixture.checkResultByFile("jam_external_beans_rename_after.xml");
        myFixture.checkResultByFile("jam_external_beans_rename_no_java_config.xml", "jam_external_beans_rename_no_java_config_after.xml", true);
        myFixture.checkResultByFile("JavaConfigExternalBeanRename1.java", "JavaConfigExternalBeansRename1_after.java", true);
        myFixture.checkResultByFile("JavaConfigExternalBeans2.java", "JavaConfigExternalBeans2_after.java", true);
      }
    }.execute().throwException();
  }
  public void testExternalBeanRename() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();

    myFixture.configureByFiles("JavaConfigExternalBeans.java","jam_external_bean_rename1.xml","jam_external_bean_rename2.xml","JavaConfigExternalBeanRename1.java","JavaConfigExternalBeans2.java");
    fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_bean_rename1.xml"));
    fileSet.addFile(myFixture.getTempDirFixture().getFile("jam_external_bean_rename2.xml"));
    final Editor editor = myFixture.getEditor();
    final PsiFile file = myFixture.getFile();

    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);

    final PsiMethod psiMethod = (PsiMethod)element.getParent();
    assertNotNull(psiMethod);
    assertTrue(SpringJamUtils.isExternalBean(psiMethod));
    final SpringJavaExternalBean externalBean = SpringJamUtils.getExternalBean(psiMethod);
    assertNotNull(externalBean);

    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        SpringExternalBeanRenameHandler.doRename(externalBean, "externalBean_new_name", false);
        myFixture.checkResultByFile("jam_external_bean_rename1.xml", "jam_external_beans_rename_after.xml", true);
        myFixture.checkResultByFile("jam_external_bean_rename2.xml", "jam_external_beans_rename2_after.xml", true);
        myFixture.checkResultByFile("JavaConfigExternalBeanRename1.java", "JavaConfigExternalBeansRename1_after.java", true);
        myFixture.checkResultByFile("JavaConfigExternalBeans2.java", "JavaConfigExternalBeans2_after.java", true);
      }
    }.execute().throwException();
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}
