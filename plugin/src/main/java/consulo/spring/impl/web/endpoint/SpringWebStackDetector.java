package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.JavaPsiFacade;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.DumbService;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringWebStackDetector {
    private SpringWebStackDetector() {
    }

    @RequiredReadAction
    public static SpringWebStack detect(@Nullable Module module) {
        if (module == null || DumbService.isDumb(module.getProject())) {
            return SpringWebStack.NONE;
        }
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> CachedValueProvider.Result.create(
            compute(module),
            SpringWebModificationTracker.getInstance(module.getProject())
        ));
    }

    @RequiredReadAction
    private static SpringWebStack compute(Module module) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        if (facade.findClass(SpringWebConstants.DISPATCHER_SERVLET, scope) != null) {
            return SpringWebStack.SERVLET;
        }
        if (facade.findClass(SpringWebConstants.DISPATCHER_HANDLER, scope) != null) {
            return SpringWebStack.REACTIVE;
        }
        return SpringWebStack.NONE;
    }
}
