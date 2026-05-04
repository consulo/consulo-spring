/**
 * @author VISTALL
 * @since 2026-05-04
 */
module com.intellij.spring.json.impl {
    requires com.intellij.spring.api;

    requires consulo.application.content.api;
    requires consulo.language.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.project.api;
    requires consulo.virtual.file.system.api;

    requires consulo.json.api;

    requires static org.jspecify;
}
