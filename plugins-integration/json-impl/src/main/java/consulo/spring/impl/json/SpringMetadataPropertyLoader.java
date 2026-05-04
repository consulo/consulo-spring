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

import com.intellij.json.psi.*;
import consulo.content.base.BinariesOrderRootType;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.spring.boot.MetadataProperty;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads {@code META-INF/spring-configuration-metadata.json} from library JARs
 * using the JSON plugin's PSI. Loaded only when the consulo.json plugin is
 * present (gated by {@code plugin-requires.xml}).
 */
public final class SpringMetadataPropertyLoader {
    public static List<MetadataProperty> loadFromModule(Module module) {
        List<MetadataProperty> result = new ArrayList<>();
        PsiManager psiManager = PsiManager.getInstance(module.getProject());

        ModuleRootManager.getInstance(module).orderEntries().librariesOnly().forEachLibrary(library -> {
            VirtualFile[] files = library.getFiles(BinariesOrderRootType.getInstance());
            for (VirtualFile root : files) {
                VirtualFile archiveRoot = ArchiveVfsUtil.getArchiveRootForLocalFile(root);
                if (archiveRoot == null) {
                    archiveRoot = root;
                }
                VirtualFile metaFile = archiveRoot.findFileByRelativePath("META-INF/spring-configuration-metadata.json");
                if (metaFile != null) {
                    loadFromFile(psiManager, metaFile, result);
                }
            }
            return true;
        });

        return result;
    }

    private static void loadFromFile(PsiManager psiManager, VirtualFile file, List<MetadataProperty> result) {
        PsiFile psiFile = psiManager.findFile(file);
        if (!(psiFile instanceof JsonFile jsonFile)) {
            return;
        }

        JsonValue topValue = jsonFile.getTopLevelValue();
        if (!(topValue instanceof JsonObject topObject)) {
            return;
        }

        JsonProperty propertiesProp = topObject.findProperty("properties");
        if (propertiesProp == null) {
            return;
        }

        JsonValue propertiesValue = propertiesProp.getValue();
        if (!(propertiesValue instanceof JsonArray propertiesArray)) {
            return;
        }

        for (JsonValue element : propertiesArray.getValueList()) {
            if (!(element instanceof JsonObject propObj)) {
                continue;
            }

            String name = getStringValue(propObj, "name");
            if (name == null) {
                continue;
            }

            String type = getStringValue(propObj, "type");
            String description = getStringValue(propObj, "description");

            result.add(new MetadataProperty(name, type, description));
        }
    }

    private static @Nullable String getStringValue(JsonObject obj, String propertyName) {
        JsonProperty prop = obj.findProperty(propertyName);
        if (prop == null) {
            return null;
        }
        JsonValue value = prop.getValue();
        if (value instanceof JsonStringLiteral stringLiteral) {
            return stringLiteral.getValue();
        }
        return null;
    }

    private SpringMetadataPropertyLoader() {
    }
}
