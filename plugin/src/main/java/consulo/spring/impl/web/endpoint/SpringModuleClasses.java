package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.JavaPsiFacade;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.DumbService;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringModuleClasses {
    private SpringModuleClasses() {
    }

    @RequiredReadAction
    public static boolean has(@Nullable Module module, String qualifiedName) {
        if (module == null || DumbService.isDumb(module.getProject())) {
            return false;
        }
        Map<String, Boolean> cache = CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> CachedValueProvider.Result.create(
            new ConcurrentHashMap<String, Boolean>(),
            SpringWebModificationTracker.getInstance(module.getProject())
        ));
        Boolean cached = cache.get(qualifiedName);
        if (cached != null) {
            return cached;
        }
        boolean found = JavaPsiFacade.getInstance(module.getProject())
            .findClass(qualifiedName, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)) != null;
        cache.put(qualifiedName, found);
        return found;
    }
}
