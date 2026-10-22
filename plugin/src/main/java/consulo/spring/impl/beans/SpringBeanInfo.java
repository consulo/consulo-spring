package consulo.spring.impl.beans;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPsiElementPointer;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Snapshot of one bean definition, built under read action. Equality is by identity of the definition
 * (module, context, kind, name, type), so a tree can find the same bean again after a rebuild.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringBeanInfo {
    private final String myModuleName;
    private final String myContextId;
    private final String myContextName;
    private final String myName;
    private final List<String> myAliases;
    private final SpringBeanKind myKind;
    private final @Nullable String myTypeName;
    private final String myScope;
    private final boolean myPrimary;
    private final boolean myLazy;
    private final List<String> myProfiles;
    private final List<String> myConditions;
    private final boolean myProfileActive;
    private final @Nullable String myLocation;
    private final @Nullable SmartPsiElementPointer<PsiElement> myPointer;

    public SpringBeanInfo(String moduleName,
                          String contextId,
                          String contextName,
                          String name,
                          List<String> aliases,
                          SpringBeanKind kind,
                          @Nullable String typeName,
                          String scope,
                          boolean primary,
                          boolean lazy,
                          List<String> profiles,
                          List<String> conditions,
                          boolean profileActive,
                          @Nullable String location,
                          @Nullable SmartPsiElementPointer<PsiElement> pointer) {
        myModuleName = moduleName;
        myContextId = contextId;
        myContextName = contextName;
        myName = name;
        myAliases = List.copyOf(aliases);
        myKind = kind;
        myTypeName = typeName;
        myScope = scope;
        myPrimary = primary;
        myLazy = lazy;
        myProfiles = List.copyOf(profiles);
        myConditions = List.copyOf(conditions);
        myProfileActive = profileActive;
        myLocation = location;
        myPointer = pointer;
    }

    public String getModuleName() {
        return myModuleName;
    }

    public String getContextId() {
        return myContextId;
    }

    public String getContextName() {
        return myContextName;
    }

    public String getName() {
        return myName;
    }

    public List<String> getAliases() {
        return myAliases;
    }

    public SpringBeanKind getKind() {
        return myKind;
    }

    public @Nullable String getTypeName() {
        return myTypeName;
    }

    public @Nullable String getShortTypeName() {
        if (myTypeName == null) {
            return null;
        }
        int index = myTypeName.lastIndexOf('.');
        return index < 0 ? myTypeName : myTypeName.substring(index + 1);
    }

    public String getScope() {
        return myScope;
    }

    public boolean isPrimary() {
        return myPrimary;
    }

    public boolean isLazy() {
        return myLazy;
    }

    public List<String> getProfiles() {
        return myProfiles;
    }

    public List<String> getConditions() {
        return myConditions;
    }

    /**
     * @return false when the bean has {@code @Profile} expressions and none of them matches the active profiles
     */
    public boolean isProfileActive() {
        return myProfileActive;
    }

    /**
     * @return where the bean is declared - the configuration class of a {@code @Bean} method, or the XML file name
     */
    public @Nullable String getLocation() {
        return myLocation;
    }

    public @Nullable SmartPsiElementPointer<PsiElement> getPointer() {
        return myPointer;
    }

    @RequiredReadAction
    public @Nullable PsiElement getElement() {
        return myPointer == null ? null : myPointer.getElement();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SpringBeanInfo that
            && myModuleName.equals(that.myModuleName)
            && myContextId.equals(that.myContextId)
            && myKind == that.myKind
            && myName.equals(that.myName)
            && Objects.equals(myTypeName, that.myTypeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myModuleName, myContextId, myKind, myName, myTypeName);
    }

    @Override
    public String toString() {
        return myName;
    }
}
