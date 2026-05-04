/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.spring.boot;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.Module;

import java.util.List;

/**
 * Pluggable contributor for Spring Boot configuration property keys.
 * <p>
 * Concrete implementations live in optional sub-plugins (json, yaml) so that
 * the main spring plugin can be loaded even when the corresponding format
 * plugin is not installed. Plugin gating is done via
 * {@code META-INF/plugin-requires.xml} on each impl jar.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface SpringConfigPropertyContributor {
    ExtensionPointName<SpringConfigPropertyContributor> EP_NAME = ExtensionPointName.create(SpringConfigPropertyContributor.class);

    /**
     * Returns true if this contributor recognises the given config file
     * (e.g. {@code application.yml}, {@code application.json}). Used to
     * route file-level lookups to the right contributor.
     */
    boolean accepts(PsiFile psiFile);

    /**
     * Resolve a dotted property key inside a single config file PSI tree.
     */
    void resolveKey(PsiFile psiFile, String key, List<PsiElement> results);

    /**
     * Collect all property keys defined inside a single config file PSI tree.
     */
    void collectKeys(PsiFile psiFile, List<String> keys);

    /**
     * Optionally contribute property metadata loaded from elsewhere
     * (e.g. {@code spring-configuration-metadata.json} in library jars).
     * The default returns an empty list.
     */
    default List<MetadataProperty> loadMetadata(Module module) {
        return List.of();
    }
}
