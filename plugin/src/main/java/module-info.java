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
    
    exports com.intellij.spring.impl;
    exports com.intellij.spring.impl.ide;
    exports com.intellij.spring.impl.ide.aop;
    exports com.intellij.spring.impl.ide.constants;
    exports com.intellij.spring.impl.ide.facet;
    exports com.intellij.spring.impl.ide.factories;
    exports com.intellij.spring.impl.ide.factories.resolvers;
    exports com.intellij.spring.impl.ide.gutter;
    exports com.intellij.spring.impl.ide.inject;
    exports com.intellij.spring.impl.ide.java;
    exports com.intellij.spring.impl.ide.java.providers;
    exports com.intellij.spring.impl.ide.metadata;
    exports com.intellij.spring.impl.ide.model;
    exports com.intellij.spring.impl.ide.model.actions;
    exports com.intellij.spring.impl.ide.model.actions.create;
    exports com.intellij.spring.impl.ide.model.actions.generate;
    exports com.intellij.spring.impl.ide.model.actions.patterns;
    exports com.intellij.spring.impl.ide.model.actions.patterns.aop;
    exports com.intellij.spring.impl.ide.model.actions.patterns.dataAccess;
    exports com.intellij.spring.impl.ide.model.actions.patterns.factoryBeans;
    exports com.intellij.spring.impl.ide.model.actions.patterns.frameworks;
    exports com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;
    exports com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util;
    exports com.intellij.spring.impl.ide.model.actions.patterns.integration;
    exports com.intellij.spring.impl.ide.model.actions.patterns.osgi;
    exports com.intellij.spring.impl.ide.model.actions.patterns.webflow;
    exports com.intellij.spring.impl.ide.model.context;
    exports com.intellij.spring.impl.ide.model.converters;
    exports com.intellij.spring.impl.ide.model.gotoSymbol;
    exports com.intellij.spring.impl.ide.model.highlighting;
    exports com.intellij.spring.impl.ide.model.highlighting.jam;
    exports com.intellij.spring.impl.ide.model.intentions;
    exports com.intellij.spring.impl.ide.model.jam;
    exports com.intellij.spring.impl.ide.model.jam.javaConfig;
    exports com.intellij.spring.impl.ide.model.jam.qualifiers;
    exports com.intellij.spring.impl.ide.model.jam.stereotype;
    exports com.intellij.spring.impl.ide.model.jam.utils;
    exports com.intellij.spring.impl.ide.model.properties;
    exports com.intellij.spring.impl.ide.model.structure;
    exports com.intellij.spring.impl.ide.model.values;
    exports com.intellij.spring.impl.ide.model.values.converters;
    exports com.intellij.spring.impl.ide.model.xml;
    exports com.intellij.spring.impl.ide.model.xml.aop;
    exports com.intellij.spring.impl.ide.model.xml.beans;
    exports com.intellij.spring.impl.ide.model.xml.context;
    exports com.intellij.spring.impl.ide.model.xml.custom;
    exports com.intellij.spring.impl.ide.model.xml.jee;
    exports com.intellij.spring.impl.ide.model.xml.jms;
    exports com.intellij.spring.impl.ide.model.xml.lang;
    exports com.intellij.spring.impl.ide.model.xml.tool;
    exports com.intellij.spring.impl.ide.model.xml.tx;
    exports com.intellij.spring.impl.ide.model.xml.util;
    exports com.intellij.spring.impl.ide.refactoring;
    exports com.intellij.spring.impl.ide.references;
    exports com.intellij.spring.impl.ide.schemas;
    exports com.intellij.spring.impl.ide.usages;
    exports com.intellij.spring.impl.model;
    exports com.intellij.spring.impl.model.aop;
    exports com.intellij.spring.impl.model.aop.psi;
    exports com.intellij.spring.impl.model.beans;
    exports com.intellij.spring.impl.model.context;
    exports com.intellij.spring.impl.model.jee;
    exports com.intellij.spring.impl.model.lang;
    exports com.intellij.spring.impl.model.tx;
    exports com.intellij.spring.impl.model.util;
    exports consulo.spring.impl;
    exports consulo.spring.impl.boot;
    exports consulo.spring.impl.boot.domOverAnnotation;
    exports consulo.spring.impl.boot.jam;
    exports consulo.spring.impl.boot.properties;
    exports consulo.spring.impl.context;
    exports consulo.spring.impl.context.widget;
    exports consulo.spring.impl.dom;
    exports consulo.spring.impl.icon;
    exports consulo.spring.impl.model;
    exports consulo.spring.impl.module.extension;
    exports consulo.spring.impl.toolWindow;
    exports consulo.spring.impl.toolWindow.tree.node;
}
