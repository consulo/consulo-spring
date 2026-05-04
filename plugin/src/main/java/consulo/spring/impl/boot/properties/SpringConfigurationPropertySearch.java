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

package consulo.spring.impl.boot.properties;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.project.Project;
import consulo.spring.boot.MetadataProperty;
import consulo.spring.boot.SpringConfigPropertyContributor;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Project-scoped service for resolving Spring Boot configuration properties.
 * <p>
 * Searches {@code application.properties} natively, and delegates to
 * {@link SpringConfigPropertyContributor} extensions for non-properties
 * formats (yaml/json) so that those format plugins remain optional.
 * Library-jar metadata ({@code spring-configuration-metadata.json}) is
 * also pulled in via the contributor extensions.
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class SpringConfigurationPropertySearch {
    private static final String[] CONFIG_FILENAMES = {
        "application.properties",
        "application.yml",
        "application.yaml",
        "application.json"
    };

    private static final String[] CONFIG_DIRS = {"", "config/"};

    private final Project myProject;

    @Inject
    public SpringConfigurationPropertySearch(Project project) {
        myProject = project;
    }

    public static SpringConfigurationPropertySearch getInstance(Project project) {
        return project.getInstance(SpringConfigurationPropertySearch.class);
    }

    /**
     * Resolve a property key to its definition(s) in config files.
     * Returns {@link IProperty} elements for {@code .properties} files and
     * format-specific PSI for other formats (e.g. {@code YAMLKeyValue}).
     */
    public List<PsiElement> resolvePropertyKey(String key, @Nullable Module module) {
        if (module == null) {
            return List.of();
        }

        List<PsiElement> results = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(myProject);

        for (VirtualFile configFile : findConfigFiles(module)) {
            PsiFile psiFile = psiManager.findFile(configFile);
            if (psiFile == null) {
                continue;
            }

            if (psiFile instanceof PropertiesFile propertiesFile) {
                List<? extends IProperty> properties = propertiesFile.findPropertiesByKey(key);
                for (IProperty property : properties) {
                    results.add(property.getPsiElement());
                }
                continue;
            }

            for (SpringConfigPropertyContributor contributor : SpringConfigPropertyContributor.EP_NAME.getExtensions()) {
                if (contributor.accepts(psiFile)) {
                    contributor.resolveKey(psiFile, key, results);
                }
            }
        }

        return results;
    }

    /**
     * Get all property keys from config files + metadata for completion.
     */
    public List<String> getAllPropertyKeys(@Nullable Module module) {
        if (module == null) {
            return List.of();
        }

        List<String> keys = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(myProject);

        for (VirtualFile configFile : findConfigFiles(module)) {
            PsiFile psiFile = psiManager.findFile(configFile);
            if (psiFile == null) {
                continue;
            }

            if (psiFile instanceof PropertiesFile propertiesFile) {
                for (IProperty property : propertiesFile.getProperties()) {
                    String propKey = property.getKey();
                    if (propKey != null && !propKey.isEmpty()) {
                        keys.add(propKey);
                    }
                }
                continue;
            }

            for (SpringConfigPropertyContributor contributor : SpringConfigPropertyContributor.EP_NAME.getExtensions()) {
                if (contributor.accepts(psiFile)) {
                    contributor.collectKeys(psiFile, keys);
                }
            }
        }

        // library-side metadata (spring-configuration-metadata.json), supplied by the json sub-plugin if installed
        for (SpringConfigPropertyContributor contributor : SpringConfigPropertyContributor.EP_NAME.getExtensions()) {
            for (MetadataProperty metaProp : contributor.loadMetadata(module)) {
                keys.add(metaProp.name());
            }
        }

        return keys;
    }

    private List<VirtualFile> findConfigFiles(Module module) {
        List<VirtualFile> files = new ArrayList<>();
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

        for (VirtualFile root : sourceRoots) {
            for (String dir : CONFIG_DIRS) {
                VirtualFile configDir = dir.isEmpty() ? root : root.findChild("config");
                if (configDir == null) {
                    continue;
                }

                for (String filename : CONFIG_FILENAMES) {
                    VirtualFile file = configDir.findChild(filename);
                    if (file != null) {
                        files.add(file);
                    }
                }

                // profile-specific files
                for (VirtualFile child : configDir.getChildren()) {
                    String name = child.getName();
                    if (name.startsWith("application-") &&
                        (name.endsWith(".properties") || name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".json"))) {
                        files.add(child);
                    }
                }
            }
        }

        return files;
    }
}
