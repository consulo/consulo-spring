/**
 * @author VISTALL
 * @since 05/02/2023
 */
open module com.intellij.spring {
  requires consulo.ide.api;
  requires consulo.ide.impl;

  requires consulo.application.api;
  requires consulo.application.content.api;
  requires consulo.application.ui.api;
  requires consulo.base.icon.library;
  requires consulo.base.localize.library;
  requires consulo.code.editor.api;
  requires consulo.component.api;
  requires consulo.configurable.api;
  requires consulo.datacontext.api;
  requires consulo.disposer.api;
  requires consulo.document.api;
  requires consulo.execution.api;
  requires consulo.file.chooser.api;
  requires consulo.file.editor.api;
  requires consulo.file.template.api;
  requires consulo.find.api;

  requires consulo.language.api;
  requires consulo.language.impl;
  requires consulo.language.code.style.api;
  requires consulo.language.editor.api;
  requires consulo.language.editor.refactoring.api;
  requires consulo.language.editor.ui.api;

  requires consulo.localize.api;
  requires consulo.logging.api;

  requires consulo.module.api;
  requires consulo.module.content.api;

  requires consulo.navigation.api;
  requires consulo.platform.api;
  requires consulo.process.api;

  requires consulo.project.api;
  requires consulo.project.content.api;
  requires consulo.project.ui.api;
  requires consulo.project.ui.view.api;

  requires consulo.ui.api;
  requires consulo.ui.ex.api;
  requires consulo.ui.ex.awt.api;
  requires consulo.usage.api;

  requires consulo.util.collection;
  requires consulo.util.concurrent;
  requires consulo.util.dataholder;
  requires consulo.util.io;
  requires consulo.util.jdom;
  requires consulo.util.lang;
  requires consulo.util.xml.serializer;

  requires consulo.virtual.file.system.api;
  requires consulo.virtual.file.status.api;

  requires com.intellij.aop;
  requires com.intellij.spring.java.ex.impl;
  requires com.intellij.spring.spel.language.api;
  requires com.intellij.spring.spel.language.impl;
  requires com.intellij.spring.api;

  requires consulo.java;
  requires consulo.java.properties.impl;
  requires com.intellij.properties;

  requires com.intellij.xml;
  requires com.intellij.xml.api;
  requires com.intellij.xml.dom.api;
  requires com.intellij.xml.editor.api;

  requires asm;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;
}
