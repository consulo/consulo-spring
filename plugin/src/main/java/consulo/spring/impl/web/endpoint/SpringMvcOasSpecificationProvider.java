package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.RecursionManager;
import consulo.endpoint.oas.OasExportUtil;
import consulo.endpoint.oas.OasSpecificationProvider;
import consulo.endpoint.oas.OpenApiSpecification;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ExtensionImpl
public final class SpringMvcOasSpecificationProvider implements OasSpecificationProvider {
    @Override
    @RequiredReadAction
    public @Nullable OpenApiSpecification getOasSpecification(UrlTargetInfo urlTargetInfo) {
        if (!(urlTargetInfo instanceof SpringMvcUrlTargetInfo)) {
            return null;
        }
        return RecursionManager.doPreventingRecursion(urlTargetInfo, false, () -> OasExportUtil.getSpecificationByUrls(List.of(urlTargetInfo)));
    }

    @Override
    @RequiredReadAction
    public @Nullable VirtualFile getOasSpecificationFile(UrlTargetInfo urlTargetInfo, List<String> prefixes) {
        return null;
    }
}
