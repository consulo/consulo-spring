/**
 * @author VISTALL
 * @since 2026-10-18
 */
module com.intellij.spring.graphql.impl {
    requires consulo.application.api;
    requires consulo.base.icon.library;
    requires consulo.component.api;
    requires consulo.endpoint.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.language.editor.ui.api;
    requires consulo.localize.api;
    requires consulo.module.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;

    requires consulo.java.language.api;
    requires consulo.java.indexing.api;

    requires consulo.graphql.core;

    requires static org.jspecify;
}
