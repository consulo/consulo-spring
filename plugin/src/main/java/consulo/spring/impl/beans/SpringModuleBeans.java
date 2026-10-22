package consulo.spring.impl.beans;

import consulo.module.Module;

import java.util.List;
import java.util.Set;

/**
 * Everything the bean view shows for one module with the Spring extension.
 *
 * @param activeProfiles         the profiles in effect - {@code spring.profiles.active}, or {@code default} when it is not set
 * @param activeProfilesExplicit whether {@code spring.profiles.active} is set
 * @author VISTALL
 * @since 2026-10-22
 */
public record SpringModuleBeans(Module module,
                                List<SpringContextInfo> contexts,
                                List<SpringProfileInfo> profiles,
                                Set<String> activeProfiles,
                                boolean activeProfilesExplicit) {
    public String getModuleName() {
        return module.getName();
    }

    public int getBeanCount() {
        int count = 0;
        for (SpringContextInfo context : contexts) {
            count += context.getBeans().size();
        }
        return count;
    }
}
