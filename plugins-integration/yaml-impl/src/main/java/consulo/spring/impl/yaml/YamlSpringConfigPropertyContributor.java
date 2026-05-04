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
package consulo.spring.impl.yaml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.spring.boot.SpringConfigPropertyContributor;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.List;

/**
 * Resolves Spring Boot property keys inside YAML config files
 * ({@code application.yml}, {@code application.yaml}). Loaded only when the
 * {@code org.jetbrains.plugins.yaml} plugin is present
 * (gated by {@code plugin-requires.xml}).
 */
@ExtensionImpl
public class YamlSpringConfigPropertyContributor implements SpringConfigPropertyContributor {
    @Override
    public boolean accepts(PsiFile psiFile) {
        return psiFile instanceof YAMLFile;
    }

    @Override
    public void resolveKey(PsiFile psiFile, String key, List<PsiElement> results) {
        if (!(psiFile instanceof YAMLFile yamlFile)) {
            return;
        }
        YAMLKeyValue keyValue = YAMLUtil.getQualifiedKeyInFile(yamlFile, key.split("\\."));
        if (keyValue != null) {
            results.add(keyValue);
        }
    }

    @Override
    public void collectKeys(PsiFile psiFile, List<String> keys) {
        if (!(psiFile instanceof YAMLFile)) {
            return;
        }
        collectRecursive(psiFile, keys);
    }

    private static void collectRecursive(PsiElement element, List<String> keys) {
        if (element instanceof YAMLKeyValue keyValue) {
            // only emit leaf keys (scalar value, not nested mapping)
            if (keyValue.getValue() instanceof YAMLScalar) {
                String fullName = YAMLUtil.getConfigFullName(keyValue);
                if (!fullName.isEmpty()) {
                    keys.add(fullName);
                }
            }
        }
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            collectRecursive(child, keys);
        }
    }
}
