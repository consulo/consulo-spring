package consulo.spring.impl.beans;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.ScopedElement;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.spring.impl.boot.properties.SpringConfigurationPropertySearch;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.spring.impl.web.endpoint.SpringConfigValues;
import consulo.spring.impl.web.endpoint.SpringRequestMappingSearch;
import consulo.ui.image.Image;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.language.psi.XmlTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the static bean snapshot of a module from the Spring model: its contexts, their beans and the profiles.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringBeansCollector {
    public static final String DEFAULT_PROFILE = "default";

    private static final String PROFILE = "org.springframework.context.annotation.Profile";
    private static final String SCOPE = "org.springframework.context.annotation.Scope";
    private static final String PRIMARY = "org.springframework.context.annotation.Primary";
    private static final String LAZY = "org.springframework.context.annotation.Lazy";
    private static final String CONDITIONAL = "org.springframework.context.annotation.Conditional";

    private static final Map<String, String> SCOPE_ANNOTATIONS = Map.of(
        "org.springframework.web.context.annotation.RequestScope", "request",
        "org.springframework.web.context.annotation.SessionScope", "session",
        "org.springframework.web.context.annotation.ApplicationScope", "application"
    );

    private static final String[] CONFIGURATION_ANNOTATIONS = {
        "org.springframework.context.annotation.Configuration",
        "org.springframework.boot.SpringBootConfiguration",
        "org.springframework.boot.autoconfigure.SpringBootApplication",
        "org.springframework.boot.autoconfigure.AutoConfiguration",
    };

    private static final String[] CONTROLLER_ANNOTATIONS = {
        "org.springframework.stereotype.Controller",
        "org.springframework.web.bind.annotation.RestController",
        "org.springframework.web.bind.annotation.ControllerAdvice",
        "org.springframework.web.bind.annotation.RestControllerAdvice",
    };

    private static final String[] SERVICE_ANNOTATIONS = {"org.springframework.stereotype.Service"};

    private static final String[] REPOSITORY_ANNOTATIONS = {"org.springframework.stereotype.Repository"};

    private SpringBeansCollector() {
    }

    @RequiredReadAction
    public static List<SpringModuleBeans> collect(Project project) {
        List<SpringModuleBeans> result = new ArrayList<>();
        for (Module module : ModuleManager.getInstance(project).getSortedModules()) {
            SpringModuleBeans beans = collect(module);
            if (beans != null) {
                result.add(beans);
            }
        }
        return result;
    }

    @RequiredReadAction
    public static @Nullable SpringModuleBeans collect(Module module) {
        if (module.getExtension(SpringModuleExtension.class) == null) {
            return null;
        }
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> CachedValueProvider.Result.create(
            build(module),
            PsiModificationTracker.MODIFICATION_COUNT
        ));
    }

    @RequiredReadAction
    private static SpringModuleBeans build(Module module) {
        String moduleName = module.getName();

        String activeValue = SpringConfigValues.readValue(module, "spring.profiles.active");
        Set<String> activeProfiles = new LinkedHashSet<>();
        if (activeValue != null) {
            for (String profile : StringUtil.split(activeValue, ",")) {
                String trimmed = profile.trim();
                if (!trimmed.isEmpty()) {
                    activeProfiles.add(trimmed);
                }
            }
        }
        boolean explicit = !activeProfiles.isEmpty();
        if (!explicit) {
            activeProfiles.add(DEFAULT_PROFILE);
        }

        SmartPointerManager pointerManager = SmartPointerManager.getInstance(module.getProject());

        List<SpringContextInfo> contexts = new ArrayList<>();
        Map<String, Integer> profileUsages = new LinkedHashMap<>();
        for (SpringModel model : SpringManager.getInstance(module.getProject()).getAllModels(module)) {
            SpringFileSet fileSet = model.getFileSet();
            String contextId = fileSet != null ? fileSet.getId() : model.getId();
            String contextName = fileSet != null ? fileSet.getName() : model.getId();
            Image contextIcon = fileSet != null && fileSet.getIcon() != null ? fileSet.getIcon() : SpringImplIconGroup.springconfig();

            Set<CommonSpringBean> seen = new LinkedHashSet<>();
            List<SpringBeanInfo> beans = new ArrayList<>();
            for (SpringBaseBeanPointer pointer : model.getAllCommonBeans(false)) {
                if (!pointer.isValid()) {
                    continue;
                }
                CommonSpringBean bean = pointer.getSpringBean();
                if (!seen.add(bean)) {
                    continue;
                }

                SpringBeanInfo info = createInfo(moduleName, contextId, contextName, pointer, bean, activeProfiles, pointerManager);
                beans.add(info);

                List<String> names = new ArrayList<>();
                for (String expression : info.getProfiles()) {
                    SpringProfileExpression.collectNames(expression, names);
                }
                for (String name : names) {
                    profileUsages.merge(name, 1, Integer::sum);
                }
            }
            contexts.add(new SpringContextInfo(moduleName, contextId, contextName, contextIcon, beans));
        }

        Map<String, List<VirtualFile>> profileFiles = new LinkedHashMap<>();
        for (VirtualFile file : SpringConfigurationPropertySearch.getInstance(module.getProject()).findConfigFiles(module)) {
            String profile = getProfileOfConfigFile(file);
            if (profile != null) {
                profileFiles.computeIfAbsent(profile, k -> new ArrayList<>()).add(file);
            }
        }

        Set<String> profileNames = new LinkedHashSet<>();
        if (explicit) {
            profileNames.addAll(activeProfiles);
        }
        profileNames.addAll(profileFiles.keySet());
        profileNames.addAll(profileUsages.keySet());

        List<SpringProfileInfo> profiles = new ArrayList<>();
        for (String name : profileNames) {
            profiles.add(new SpringProfileInfo(
                moduleName,
                name,
                activeProfiles.contains(name),
                profileUsages.getOrDefault(name, 0),
                profileFiles.getOrDefault(name, List.of())
            ));
        }

        return new SpringModuleBeans(module, contexts, profiles, Set.copyOf(activeProfiles), explicit);
    }

    /**
     * @return {@code dev} for {@code application-dev.yml}, null for a file which is not profile specific
     */
    public static @Nullable String getProfileOfConfigFile(VirtualFile file) {
        String name = file.getNameWithoutExtension();
        if (!name.startsWith("application-")) {
            return null;
        }
        String profile = name.substring("application-".length());
        return profile.isEmpty() ? null : profile;
    }

    @RequiredReadAction
    private static SpringBeanInfo createInfo(String moduleName,
                                             String contextId,
                                             String contextName,
                                             SpringBaseBeanPointer pointer,
                                             CommonSpringBean bean,
                                             Set<String> activeProfiles,
                                             SmartPointerManager pointerManager) {
        PsiElement element = bean.getIdentifyingPsiElement();
        PsiModifierListOwner owner = element instanceof PsiModifierListOwner listOwner ? listOwner : null;
        PsiClass containingClass = element instanceof PsiMethod method ? method.getContainingClass() : null;

        List<String> profiles = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        String scope = "singleton";
        boolean primary = false;
        boolean lazy = false;
        String location = null;
        SpringBeanKind kind;

        if (bean instanceof DomSpringBean domBean) {
            kind = SpringBeanKind.XML;
            XmlTag tag = domBean.getXmlTag();
            if (bean instanceof ScopedElement scoped) {
                String value = scoped.getScope().getStringValue();
                if (!StringUtil.isEmptyOrSpaces(value)) {
                    scope = value;
                }
            }
            if (tag != null) {
                primary = "true".equals(tag.getAttributeValue("primary"));
                lazy = "true".equals(tag.getAttributeValue("lazy-init"));
                collectXmlProfiles(tag, profiles);
            }
            PsiFile file = domBean.getXmlElement() == null ? null : domBean.getXmlElement().getContainingFile();
            location = file == null ? null : file.getName();
        }
        else {
            kind = element instanceof PsiMethod ? SpringBeanKind.BEAN_METHOD : getClassKind(element);
            if (containingClass != null) {
                location = containingClass.getName();
                readProfiles(containingClass, profiles);
                readConditions(containingClass, conditions);
            }
            if (owner != null) {
                readProfiles(owner, profiles);
                readConditions(owner, conditions);
                scope = readScope(owner, scope);
                primary = AnnotationUtil.isAnnotated(owner, PRIMARY, 0);
                lazy = AnnotationUtil.isAnnotated(owner, LAZY, 0);
            }
        }

        String name = pointer.getName();
        if (StringUtil.isEmptyOrSpaces(name)) {
            name = bean.getBeanName();
        }
        if (StringUtil.isEmptyOrSpaces(name)) {
            PsiClass beanClass = pointer.getBeanClass();
            name = beanClass != null && beanClass.getName() != null ? StringUtil.decapitalize(beanClass.getName()) : "?";
        }

        List<String> aliases = new ArrayList<>();
        for (String alias : pointer.getAliases()) {
            if (!StringUtil.isEmptyOrSpaces(alias) && !alias.equals(name)) {
                aliases.add(alias);
            }
        }

        String typeName = null;
        PsiClass[] effectiveTypes = pointer.getEffectiveBeanType();
        PsiClass beanClass = effectiveTypes.length > 0 ? effectiveTypes[0] : pointer.getBeanClass();
        if (beanClass != null) {
            typeName = beanClass.getQualifiedName();
        }

        SmartPsiElementPointer<PsiElement> psiPointer = element == null ? null : pointerManager.createSmartPsiElementPointer(element);

        return new SpringBeanInfo(
            moduleName,
            contextId,
            contextName,
            name,
            aliases,
            kind,
            typeName,
            scope,
            primary,
            lazy,
            profiles,
            conditions,
            SpringProfileExpression.matchesAny(profiles, activeProfiles),
            location,
            psiPointer
        );
    }

    @RequiredReadAction
    private static SpringBeanKind getClassKind(@Nullable PsiElement element) {
        if (!(element instanceof PsiClass psiClass)) {
            return SpringBeanKind.OTHER;
        }
        if (isAnnotatedWithAny(psiClass, CONFIGURATION_ANNOTATIONS)) {
            return SpringBeanKind.CONFIGURATION;
        }
        if (isAnnotatedWithAny(psiClass, CONTROLLER_ANNOTATIONS)) {
            return SpringBeanKind.CONTROLLER;
        }
        if (isAnnotatedWithAny(psiClass, SERVICE_ANNOTATIONS)) {
            return SpringBeanKind.SERVICE;
        }
        if (isAnnotatedWithAny(psiClass, REPOSITORY_ANNOTATIONS)) {
            return SpringBeanKind.REPOSITORY;
        }
        return SpringBeanKind.COMPONENT;
    }

    @RequiredReadAction
    private static boolean isAnnotatedWithAny(PsiModifierListOwner owner, String[] annotations) {
        for (String annotation : annotations) {
            if (AnnotationUtil.isAnnotated(owner, annotation, 0)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static void readProfiles(PsiModifierListOwner owner, List<String> profiles) {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(owner, PROFILE);
        if (annotation == null) {
            return;
        }
        for (String profile : SpringRequestMappingSearch.readStrings(annotation, "value")) {
            if (!profiles.contains(profile)) {
                profiles.add(profile);
            }
        }
    }

    @RequiredReadAction
    private static void readConditions(PsiModifierListOwner owner, List<String> conditions) {
        PsiModifierList modifierList = owner.getModifierList();
        if (modifierList == null) {
            return;
        }
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) {
                continue;
            }
            String shortName = StringUtil.getShortName(qualifiedName);
            if (CONDITIONAL.equals(qualifiedName) || shortName.startsWith("ConditionalOn")) {
                String text = annotation.getText().replaceAll("\\s+", " ");
                if (!conditions.contains(text)) {
                    conditions.add(text);
                }
            }
        }
    }

    @RequiredReadAction
    private static String readScope(PsiModifierListOwner owner, String defaultScope) {
        for (Map.Entry<String, String> entry : SCOPE_ANNOTATIONS.entrySet()) {
            if (AnnotationUtil.isAnnotated(owner, entry.getKey(), 0)) {
                return entry.getValue();
            }
        }
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(owner, SCOPE);
        if (annotation == null) {
            return defaultScope;
        }
        for (String attribute : new String[]{"value", "scopeName"}) {
            List<String> values = SpringRequestMappingSearch.readStrings(annotation, attribute);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }
        return defaultScope;
    }

    private static void collectXmlProfiles(XmlTag tag, Collection<String> profiles) {
        XmlTag current = tag.getParentTag();
        while (current != null) {
            if ("beans".equals(current.getLocalName())) {
                String value = current.getAttributeValue("profile");
                if (value != null) {
                    for (String profile : StringUtil.tokenize(value, ", ")) {
                        if (!profiles.contains(profile)) {
                            profiles.add(profile);
                        }
                    }
                }
            }
            current = current.getParentTag();
        }
    }
}
