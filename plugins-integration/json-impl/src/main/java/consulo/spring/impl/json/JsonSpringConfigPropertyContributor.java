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
package consulo.spring.impl.json;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.Module;
import consulo.spring.boot.MetadataProperty;
import consulo.spring.boot.SpringConfigPropertyContributor;

import java.util.List;

/**
 * Contributes Spring Boot property metadata loaded from
 * {@code META-INF/spring-configuration-metadata.json} files inside library JARs.
 * <p>
 * Spring Boot also supports {@code application.json} as a config source, but
 * Consulo does not currently surface that as standard PSI; for now this
 * contributor only contributes JAR-side metadata via {@link #loadMetadata}.
 */
@ExtensionImpl
public class JsonSpringConfigPropertyContributor implements SpringConfigPropertyContributor {
    @Override
    public boolean accepts(PsiFile psiFile) {
        return false;
    }

    @Override
    public void resolveKey(PsiFile psiFile, String key, List<PsiElement> results) {
        // application.json is not handled at the PSI level here.
    }

    @Override
    public void collectKeys(PsiFile psiFile, List<String> keys) {
        // application.json is not handled at the PSI level here.
    }

    @Override
    public List<MetadataProperty> loadMetadata(Module module) {
        return SpringMetadataPropertyLoader.loadFromModule(module);
    }
}
