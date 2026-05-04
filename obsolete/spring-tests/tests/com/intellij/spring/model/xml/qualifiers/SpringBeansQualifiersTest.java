package com.intellij.spring.model.xml.qualifiers;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiReference;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.jam.SpringJamModel;
import com.intellij.spring.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.spring.model.jam.stereotype.SpringService;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SpringBeansQualifiersTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    addSpring_2_5_Library(moduleBuilder);
  }

   public void testQualifiersHighlighting() throws Throwable {

    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        final SpringFileSet fileSet = configureFileSet();
        myFixture.copyFileToProject("qualified_highlighting.xml"); // stereotypes with qualifiers
        fileSet.addFile(myFixture.getTempDirFixture().getFile("qualified_highlighting.xml"));

        myFixture.configureByFiles("example/FooClass.java", "FooInjection.java", "FooInjection2.java", "QualifierAnnotated.java",
                                   "QualifierAnnotatedChild.java" );

        myFixture.allowTreeAccessForFile(myFixture.getTempDirFixture().getFile("example/FooClass.java"));
        myFixture.testHighlighting(true, false, false, "Foo.java");
      }
    }.execute().throwException();
   }
  
  public void testQualifiersCompletion() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("qualified_beans.xml", "FooInjection.java", "FooInjection2.java", "QualifierAnnotated.java",
                                   "QualifierAnnotatedChild.java");

        fileSet.addFile(myFixture.getTempDirFixture().getFile("qualified_beans.xml"));

        myFixture.testCompletion("FooCompletion1.java", "FooCompletion1_after.java");
        myFixture.testCompletion("FooCompletion2.java", "FooCompletion2_after.java");
        myFixture.testCompletion("FooCompletion3.java", "FooCompletion3_after.java");
        myFixture.testCompletion("CompletionByName.java", "CompletionByName_after.java");
      }
    }.execute().throwException();

  }

  public void testQualifiersRename1() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("qualified_beans.xml", "FooInjection.java", "FooInjection2.java", "QualifierAnnotated.java",
                                   "QualifierAnnotatedChild.java");

        fileSet.addFile(myFixture.getTempDirFixture().getFile("qualified_beans.xml"));

        myFixture.testRename("FooRename1.java", "FooRename1_after.java", "f1_new");
        myFixture.checkResultByFile("qualified_beans.xml", "qualified_beans_renamed1.xml", true);
      }
    }.execute().throwException();

  }

  public void testQualifiersRename2() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("qualified_beans.xml", "FooInjection.java", "FooInjection2.java", "QualifierAnnotated.java",
                                   "QualifierAnnotatedChild.java");

        fileSet.addFile(myFixture.getTempDirFixture().getFile("qualified_beans.xml"));

        myFixture.testRename("FooRenamed2.java", "FooRenamed2_after.java", "f2_new");
        myFixture.checkResultByFile("qualified_beans.xml", "qualified_beans_renamed2.xml", true);
      }
    }.execute().throwException();
  }

  public void testQualifiersRename3() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("qualified_beans.xml", "FooInjection.java", "FooInjection2.java", "QualifierAnnotated.java",
                                   "QualifierAnnotatedChild.java");

        fileSet.addFile(myFixture.getTempDirFixture().getFile("qualified_beans.xml"));

        myFixture.testRename("FooRename3.java", "FooRename3_after.java", "f3_new");
        myFixture.checkResultByFile("qualified_beans.xml", "qualified_beans_renamed3.xml", true);
      }
    }.execute().throwException();

  }

  public void _testStereotypeQualifierRename() throws Throwable {
    //todo
    myFixture.copyFileToProject("example/FooServiceWithQualifier.java", "example/FooServiceWithQualifier.java");
    myFixture.copyFileToProject("example/FooAutowiredService.java", "example/FooAutowiredService.java");

    //myFixture.allowTreeAccessForFile(getFile("example/FooServiceWithQualifier.java"));
    myFixture.allowTreeAccessForFile(getFile(myFixture.getTempDirPath() +"/" +"example/FooAutowiredService.java"));

    myFixture.configureByFiles("example/FooServiceWithQualifier.java", "example/FooAutowiredService.java");

    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "stereotipes.xml");

    List<? extends SpringService> services = SpringJamModel.getModel(myModule).getServices();

    assertTrue(services.size() > 0);

    for (SpringService service : services) {
      SpringJamQualifier jamQualifier = service.getQualifier();
      if (jamQualifier != null) {
        for (PsiNameValuePair nameValuePair : jamQualifier.getAnnotation().getParameterList().getAttributes()) {
          //if (nameValuePair.getName().equals("name")) {
            PsiAnnotationMemberValue value = nameValuePair.getValue();
            PsiReference[] references = value.getReferences();
          //}
        }
      }
    }
    myFixture.testHighlighting(false, false, false, "example/FooServiceWithQualifier.java");
    myFixture.testRename("example/FooServiceWithQualifier.java", "example/FooServiceWithQualifier_after.java", "fooQualifier_new");
    myFixture.checkResultByFile("example/FooAutowiredService.java", "example/FooAutowiredService_after.java", true);
  }

  public void testQualifierWithoutValue() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "stereotipes.xml");
    myFixture.testHighlighting(true, false, true, "example/QualifierWithoutValue.java");
  }

  public void testAutowiredWithCustomQualifier() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "stereotipes.xml");
    myFixture.testHighlighting(true, false, true, "example/AutowiredWithCustomQualifier.java", "example/CustomQualifier.java");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/qualifiers/";
  }
}
