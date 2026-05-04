package com.intellij.spring.refactoring;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataConstants;
import consulo.ide.impl.idea.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import consulo.ide.impl.idea.refactoring.inline.InlineRefactoringActionHandler;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * @author Dmitry Avdeev
 */
public class SpringRefactoringTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  public void testIntroduceBean() throws Throwable {
    CodeInsightTestUtil.doIntentionTest(myFixture, "testIntroduceBean", new SpringIntroduceBeanIntention().getText());
  }

  @Override
  protected boolean isWithTestSources() {
    return true;
  }

  public void testMoveBean() throws Throwable {
    myFixture.configureByFiles("move-from.xml", "move-to.xml");
    final Editor editor = myFixture.getEditor();
    final int offset = editor.getCaretModel().getOffset();
    final PsiElement psiElement = myFixture.getFile().findElementAt(offset);
    final SpringBean springBean = SpringUtils.findBeanFromPsiElement(psiElement);
    final VirtualFile file = myFixture.getTempDirFixture().getFile("move-to.xml");
    assert file != null;
    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    SpringBeanMoveDialog.doMove((XmlFile)psiFile, springBean, myProject);
    myFixture.checkResultByFile("move-from_after.xml");
    myFixture.checkResultByFile("move-to.xml", "move-to_after.xml", false);
  }

  public void testInlineBean() throws Throwable {
    doInline("inlineBean");
  }

  public void testInlineParent() throws Throwable {
    doInline("inlineParent");
  }

  public void testInlineProperties() throws Throwable {
    doInline("inlineProperties");
  }

  public void testUpdateSchema() throws Throwable {
    CodeInsightTestUtil.doIntentionTest(myFixture, "testUpdateSchema", new SpringUpdateSchemaIntention().getText());    
  }

  public void testUsePNamespace() throws Throwable {
    CodeInsightTestUtil.doIntentionTest(myFixture, "usePNamespace", new UsePNamespaceIntention().getText());
    CodeInsightTestUtil.doIntentionTest(myFixture, "usePNamespaceRef", new UsePNamespaceIntention().getText());
    CodeInsightTestUtil.doIntentionTest(myFixture, "usePNamespaceValueTag", new UsePNamespaceIntention().getText());
  }

  private void doInline(final String file) throws Throwable {
    final PsiReference reference = myFixture.getReferenceAtCaretPositionWithAssertion(file + ".xml");
    final EditorEx editor = (EditorEx)myFixture.getEditor();
    DataContext dataContext = SimpleDataContext.getSimpleContext(DataConstants.PSI_ELEMENT, reference.resolve(), editor.getDataContext());
    new InlineRefactoringActionHandler().invoke(myProject, editor, myFixture.getFile(), dataContext);
    myFixture.checkResultByFile(file + "_after.xml");
  }

  @Override
  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    if (getName().equals("testMoveBean")) {
      addSpringJar(moduleBuilder);
    }
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/refactoring";    
  }
}
