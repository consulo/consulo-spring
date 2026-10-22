package consulo.spring.impl.beans;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.spring.impl.web.endpoint.SpringRequestMappingSearch;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The injection points of a bean - constructor and {@code @Bean} method parameters, {@code @Autowired} fields and
 * setters - with the beans of its context which can satisfy each of them.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringBeanDependencies {
    /**
     * @param name       the parameter or field name
     * @param typeText   the declared type as written
     * @param candidates names of the beans which match the type (and the {@code @Qualifier}, when there is one)
     */
    public record Dependency(String name, String typeText, List<String> candidates) {
    }

    private static final String[] INJECT_ANNOTATIONS = {
        "org.springframework.beans.factory.annotation.Autowired",
        "jakarta.inject.Inject",
        "javax.inject.Inject",
        "jakarta.annotation.Resource",
        "javax.annotation.Resource",
    };

    private static final String QUALIFIER = "org.springframework.beans.factory.annotation.Qualifier";

    private static final Set<String> CONTAINER_TYPES = Set.of(
        "java.util.Collection",
        "java.util.List",
        "java.util.Set",
        "java.util.Optional",
        "org.springframework.beans.factory.ObjectProvider",
        "org.springframework.beans.factory.ObjectFactory",
        "jakarta.inject.Provider",
        "javax.inject.Provider"
    );

    private SpringBeanDependencies() {
    }

    @RequiredReadAction
    public static List<Dependency> collect(Project project, SpringBeanInfo bean) {
        PsiElement element = bean.getElement();
        if (element == null) {
            return List.of();
        }

        List<PsiModifierListOwner> points = new ArrayList<>();
        if (element instanceof PsiMethod method) {
            points.addAll(List.of(method.getParameterList().getParameters()));
        }
        else if (element instanceof PsiClass psiClass) {
            PsiMethod constructor = findInjectedConstructor(psiClass);
            if (constructor != null) {
                points.addAll(List.of(constructor.getParameterList().getParameters()));
            }
            for (PsiField field : psiClass.getAllFields()) {
                if (isInjected(field)) {
                    points.add(field);
                }
            }
            for (PsiMethod method : psiClass.getAllMethods()) {
                if (!method.isConstructor() && isInjected(method)) {
                    points.addAll(List.of(method.getParameterList().getParameters()));
                }
            }
        }

        if (points.isEmpty()) {
            return List.of();
        }

        SpringModel model = findModel(project, bean);
        List<Dependency> result = new ArrayList<>();
        for (PsiModifierListOwner point : points) {
            PsiType type = point instanceof PsiParameter parameter ? parameter.getType() : ((PsiField) point).getType();
            String name = point instanceof PsiParameter parameter ? parameter.getName() : ((PsiField) point).getName();
            result.add(new Dependency(
                name == null ? "?" : name,
                type.getPresentableText(),
                model == null ? List.of() : findCandidates(model, point, type)
            ));
        }
        return result;
    }

    @RequiredReadAction
    private static @Nullable PsiMethod findInjectedConstructor(PsiClass psiClass) {
        PsiMethod[] constructors = psiClass.getConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        for (PsiMethod constructor : constructors) {
            if (isInjected(constructor)) {
                return constructor;
            }
        }
        return null;
    }

    @RequiredReadAction
    private static boolean isInjected(PsiModifierListOwner owner) {
        for (String annotation : INJECT_ANNOTATIONS) {
            if (AnnotationUtil.isAnnotated(owner, annotation, 0)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static List<String> findCandidates(SpringModel model, PsiModifierListOwner point, PsiType type) {
        PsiClass psiClass = resolveInjectedClass(type);
        if (psiClass == null) {
            return List.of();
        }

        String qualifier = null;
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(point, QUALIFIER);
        if (annotation != null) {
            List<String> values = SpringRequestMappingSearch.readStrings(annotation, "value");
            qualifier = values.isEmpty() ? null : values.get(0);
        }

        List<String> names = new ArrayList<>();
        for (SpringBaseBeanPointer candidate : model.findBeansByEffectivePsiClassWithInheritance(psiClass)) {
            String candidateName = candidate.getName();
            if (candidateName == null || names.contains(candidateName)) {
                continue;
            }
            if (qualifier != null && !qualifier.equals(candidateName) && !List.of(candidate.getAliases()).contains(qualifier)) {
                continue;
            }
            names.add(candidateName);
        }
        return names;
    }

    @RequiredReadAction
    private static @Nullable PsiClass resolveInjectedClass(PsiType type) {
        if (type instanceof PsiClassType classType) {
            PsiClass resolved = classType.resolve();
            PsiType[] parameters = classType.getParameters();
            if (resolved != null && parameters.length == 1 && CONTAINER_TYPES.contains(resolved.getQualifiedName())) {
                return PsiUtil.resolveClassInType(parameters[0]);
            }
            if (resolved != null && parameters.length == 2 && "java.util.Map".equals(resolved.getQualifiedName())) {
                return PsiUtil.resolveClassInType(parameters[1]);
            }
        }
        return PsiUtil.resolveClassInType(type);
    }

    @RequiredReadAction
    private static @Nullable SpringModel findModel(Project project, SpringBeanInfo bean) {
        Module module = ModuleManager.getInstance(project).findModuleByName(bean.getModuleName());
        if (module == null) {
            return null;
        }
        for (SpringModel model : SpringManager.getInstance(project).getAllModels(module)) {
            SpringFileSet fileSet = model.getFileSet();
            String id = fileSet != null ? fileSet.getId() : model.getId();
            if (id.equals(bean.getContextId())) {
                return model;
            }
        }
        return null;
    }
}
