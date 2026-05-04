package com.intellij.spring.model.xml.webflow;

import com.intellij.codeInspection.jsp.ELValidationInspection;
import com.intellij.idea.Bombed;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Calendar;

/**
 * User: Sergey.Vasiliev
 */
public class WebflowElTest extends SpringHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    myFixture.enableInspections(new ELValidationInspection());

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    addSpring_2_5_Library(moduleBuilder);

    //moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/", "/");

    moduleBuilder
        .addLibraryJars("webflow-jar", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "webflow_2_0.jar");
  }

  private void createFileSet() throws Throwable {
    final String path = myFixture.getTempDirPath() + File.separatorChar + "spring-beans.xml";
    final String url = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, path);
    configureFileSet().addFile(url);
  }

  @Bombed(user = "sergey.vasiliev", month = Calendar.SEPTEMBER, day = 28)
  public void testAppContextHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(true, false, true, "booking_app_context.xml", "spring-beans.xml");
  }

  public void testFlowScopeCompletion() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("booking_flow_scope_before.xml", "booking_flow_scope_after.xml", "spring-beans.xml");
  }

   @Bombed(user = "sergey.vasiliev", month = Calendar.SEPTEMBER, day = 28)
  public void testFlowScopeRename() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testRename("booking_flow_scope_rename_before.xml", "booking_flow_scope_rename_after.xml", "booking_new", "spring-beans.xml");
  }

  @Bombed(user = "sergey.vasiliev", month = Calendar.SEPTEMBER, day = 28)
  public void testFlowScopeRename2() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture
        .testRename("booking_flow_scope_rename_before2.xml", "booking_flow_scope_rename_after2.xml", "booking_new", "spring-beans.xml");
  }

  public void testFlowScopeCompletion2() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("booking_flow_scope_before2.xml", "booking_flow_scope_after2.xml", "spring-beans.xml");
  }

  public void testFlowScopeCompletion3() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("booking_flow_scope_before3.xml", "booking_flow_scope_after3.xml", "spring-beans.xml");
  }

  public void testFlowScopeCompletionVariants() throws Throwable {
    createFileSet();

    myFixture.copyDirectoryToProject("beans", "beans");
    myFixture.copyFileToProject("spring-beans.xml");

    myFixture.testCompletionVariants("booking_flow_scope_completion_variants.xml", "class", "id");
  }

  public void testFlowScopeCompletionVariants2() throws Throwable {
    createFileSet();

    myFixture.copyDirectoryToProject("beans", "beans");
    myFixture.copyFileToProject("spring-beans.xml");

    myFixture.testCompletion("booking_flow_scope_completion_variants_before.xml", "booking_flow_scope_completion_variants_after.xml");
  }

  public void testCurrentUserVarHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");
    myFixture.addClass("package java.security; public interface Principal { public String getName(); }");
    myFixture.testHighlighting(true, false, true, "booking_current_user.xml", "spring-beans.xml");
  }

  public void testPersistenceContextHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");
    myFixture.addClass("package javax.persistence; public interface EntityManager  { void persist(java.lang.Object o); }");

    myFixture.testHighlighting(true, false, true, "booking_persistence_context.xml");
  }

  public void testInputVariablesHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.configureByFiles("booking_input_variables.xml", "spring-beans.xml");
    myFixture.testHighlighting(true, false, true, "booking_input_variables.xml", "spring-beans.xml");
  }

  public void testInputVariablesRename() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testRename("booking_input_variables_before.xml", "booking_input_variables_after.xml", "holderId_new", "spring-beans.xml");
  }

  public void testVarsHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(true, false, true, "booking_vars.xml");
  }

  public void testLocalVarsHighlighting() throws Throwable {
    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testHighlighting(true, false, true, "booking_local_vars.xml");
  }

  public void testVarsRename() throws Throwable {

    createFileSet();
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testRename("booking_vars_before.xml", "booking_vars_after.xml", "booking_new", "spring-beans.xml");
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/webflow/";
  }
}
