package consulo.spring.impl.beans;

import consulo.localize.LocalizeValue;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.image.Image;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2026-10-22
 */
public enum SpringBeanKind {
    CONFIGURATION(SpringLocalize::beansKindConfiguration, SpringImplIconGroup::springjavaconfig),
    CONTROLLER(SpringLocalize::beansKindController, SpringImplIconGroup::gutterRequestmapping),
    SERVICE(SpringLocalize::beansKindService, SpringImplIconGroup::springjavabean),
    REPOSITORY(SpringLocalize::beansKindRepository, SpringImplIconGroup::springjavabean),
    COMPONENT(SpringLocalize::beansKindComponent, SpringImplIconGroup::springjavabean),
    BEAN_METHOD(SpringLocalize::beansKindBeanMethod, SpringImplIconGroup::gutterSpringbeanmethod),
    XML(SpringLocalize::beansKindXml, SpringImplIconGroup::springbean),
    OTHER(SpringLocalize::beansKindOther, SpringImplIconGroup::springbean);

    private final Supplier<LocalizeValue> myName;
    private final Supplier<Image> myIcon;

    SpringBeanKind(Supplier<LocalizeValue> name, Supplier<Image> icon) {
        myName = name;
        myIcon = icon;
    }

    public LocalizeValue getName() {
        return myName.get();
    }

    public Image getIcon() {
        return myIcon.get();
    }
}
