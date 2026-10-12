package consulo.spring.impl.module.extension.ui;

import consulo.virtualFileSystem.VirtualFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
record SpringConfigFilesSearchResult(
    Map<Object, List<VirtualFile>> groups,
    List<VirtualFile> looseFiles,
    Set<VirtualFile> checkedFiles,
    Map<Object, SpringConfigFilePresentation> presentations
) {
}
