package consulo.spring.impl.beans;

import consulo.ui.image.Image;

import java.util.List;
import java.util.Objects;

/**
 * One Spring context of a module: an XML file set or a Spring Boot application class.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringContextInfo {
    private final String myModuleName;
    private final String myId;
    private final String myName;
    private final Image myIcon;
    private final List<SpringBeanInfo> myBeans;

    public SpringContextInfo(String moduleName, String id, String name, Image icon, List<SpringBeanInfo> beans) {
        myModuleName = moduleName;
        myId = id;
        myName = name;
        myIcon = icon;
        myBeans = List.copyOf(beans);
    }

    public String getModuleName() {
        return myModuleName;
    }

    public String getId() {
        return myId;
    }

    public String getName() {
        return myName;
    }

    public Image getIcon() {
        return myIcon;
    }

    public List<SpringBeanInfo> getBeans() {
        return myBeans;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SpringContextInfo that && myModuleName.equals(that.myModuleName) && myId.equals(that.myId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myModuleName, myId);
    }

    @Override
    public String toString() {
        return myName;
    }
}
