/**
 * Spring plugin shared API surface. Extension points and shared abstractions
 * intended to be implemented by optional sub-plugins (json, yaml, ...) and
 * by external integrations live here.
 *
 * @author VISTALL
 * @since 2026-05-04
 */
module com.intellij.spring.api {
    requires consulo.component.api;
    requires consulo.language.api;
    requires consulo.module.api;
    requires consulo.virtual.file.system.api;

    requires static org.jspecify;

    exports consulo.spring.boot;
}
