/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.PsiArrayType;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.macro.MacroCallNode;
import consulo.language.editor.template.macro.MacroFactory;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class SpringTemplateBuilder {

  private final Template myTemplate;
  private final Project myProject;
  private int myCount;

  private static final Set<String> myConvertableTypes = new HashSet<String>();

  static {
    String[] classes = {"java.lang.String", "java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short",
      "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.math.BigDecimal", "java.math.BigInteger",
      "java.lang.Class", "org.springframework.core.io.Resource", "java.net.URL", "java.io.File", "java.io.InputStream", "java.util.Locale",
      "java.util.Properties"};

    myConvertableTypes.addAll(Arrays.asList(classes));
  }

  public SpringTemplateBuilder(Project project) {
    myProject = project;
    myTemplate = TemplateManager.getInstance(project).createTemplate("", "");
    myTemplate.setToReformat(true);
  }

  /**
   * @param type
   * @param model
   * @return true if closing tag needed
   */
  private boolean createValue(PsiType type, SpringModel model) {
    if (type instanceof PsiClassType) {
      if (CommonClassNames.JAVA_LANG_OBJECT.equals(type.getCanonicalText())) {
        createAttr("value");  // IDEADEV-17789
        return false;
      }
      else {
        PsiClassType psiClassType = (PsiClassType)type;
        if (SpringUtils.isAssignable(myProject, psiClassType, CommonClassNames.JAVA_UTIL_PROPERTIES)) {
          createProperties();
          return true;
        }
        else if (SpringUtils.isAssignable(myProject, psiClassType, CommonClassNames.JAVA_UTIL_MAP)) {
          createMap();
          return true;
        }
        else if (SpringUtils.isAssignable(myProject, psiClassType, CommonClassNames.JAVA_UTIL_SET)) {
          createCollection("set");
          return true;
        }
        else if (SpringUtils.isAssignable(myProject, psiClassType, CommonClassNames.JAVA_UTIL_COLLECTION)) {
          createCollection("list");
          return true;
        }
        else {
          createAttr(psiClassType, model, false);
          return false;
        }
      }
    }
    else if (type instanceof PsiArrayType) {
      createCollection("list");
      return true;
    }
    else {
      createAttr("value");
      return false;
    }
  }

  public void createValueAndClose(PsiType type, SpringModel model, @NonNls String tagName) {
    boolean closingTagNeeded = createValue(type, model);
    addTextSegment(closingTagNeeded ? "</" + tagName + ">" : "/>");
  }

  private void createAttr(PsiClassType type, SpringModel model, boolean key) {
    boolean canBeReferenced = !SpringUtils.getBeansByType(type, model).isEmpty();
    if (canBeReferenced || !isConvertable(type)) {
      createAttr(key ? "key-ref" : "ref");
    }
    else {
      createAttr(key ? "key-value" : "value");
    }
  }

  private static boolean isConvertable(PsiType type) {
    return myConvertableTypes.contains(type.getCanonicalText());
  }

  private void createMap() {
    myTemplate.addTextSegment("><map>\n<entry");
    createAttr("key");
    createAttr("value");
    myTemplate.addTextSegment("/>\n</map>\n");
  }

  private void createProperties() {
    myTemplate.addTextSegment(">\n<props>\n<prop key=\"");
    MacroCallNode node = new MacroCallNode(MacroFactory.createMacro("complete"));
    myTemplate.addVariable("PROP_KEY", node, node, true);
    myTemplate.addTextSegment("\">");
    myTemplate.addVariable("PROP_VALUE", node, node, true);
    myTemplate.addTextSegment("</prop>\n</props>");
  }

  public void createCollection(String name) {
    myTemplate.addTextSegment(">\n<" + name + ">\n");
    myTemplate.addTextSegment("<value>");
    MacroCallNode node = new MacroCallNode(MacroFactory.createMacro("complete"));
    myTemplate.addVariable(name + myCount++, node, node, true);
    myTemplate.addTextSegment("</value>\n");
    myTemplate.addTextSegment("</" + name + ">\n");
  }

  private void createAttr(String name) {
    myTemplate.addTextSegment(" " + name + "=\"");
    MacroCallNode node = new MacroCallNode(MacroFactory.createMacro("complete"));
    myTemplate.addVariable(name + myCount++, node, node, true);
    myTemplate.addTextSegment("\"");
  }

  public void addTextSegment(@NonNls String s) {
    myTemplate.addTextSegment(s);
  }

  public void startTemplate(Editor editor) {
    TemplateManager.getInstance(myProject).startTemplate(editor, myTemplate);
  }

  public static void preparePlace(Editor editor, Project project, DomElement element) {
    DomElement copy = element.createStableCopy();
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
    XmlTag tag = copy.getXmlTag();
    assert tag != null;
    int offset = tag.getTextOffset();
    editor.getDocument().deleteString(offset, tag.getTextRange().getEndOffset());
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
    editor.getCaretModel().moveToOffset(offset);
  }

  public static Editor getEditor(ProblemDescriptor descriptor) {
    PsiFile psiFile = descriptor.getPsiElement().getContainingFile();
    Project project = psiFile.getProject();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    Editor editor = FileEditorManager.getInstance(project)
                                           .openTextEditor(OpenFileDescriptorFactory.getInstance(project).builder(virtualFile).build(),
                                                           false);
    assert editor != null;
    return editor;
  }
}
