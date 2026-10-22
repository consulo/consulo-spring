package consulo.spring.impl.beans;

import consulo.virtualFileSystem.VirtualFile;

import java.util.List;
import java.util.Objects;

/**
 * A profile known in a module: named by an {@code application-<profile>} config file, by {@code @Profile}, or by
 * {@code spring.profiles.active}.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringProfileInfo {
    private final String myModuleName;
    private final String myName;
    private final boolean myActive;
    private final int myBeanCount;
    private final List<VirtualFile> myConfigFiles;

    public SpringProfileInfo(String moduleName, String name, boolean active, int beanCount, List<VirtualFile> configFiles) {
        myModuleName = moduleName;
        myName = name;
        myActive = active;
        myBeanCount = beanCount;
        myConfigFiles = List.copyOf(configFiles);
    }

    public String getModuleName() {
        return myModuleName;
    }

    public String getName() {
        return myName;
    }

    public boolean isActive() {
        return myActive;
    }

    public int getBeanCount() {
        return myBeanCount;
    }

    public List<VirtualFile> getConfigFiles() {
        return myConfigFiles;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SpringProfileInfo that && myModuleName.equals(that.myModuleName) && myName.equals(that.myName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myModuleName, myName);
    }

    @Override
    public String toString() {
        return myName;
    }
}
