package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import com.intellij.spring.impl.ide.model.xml.util.UtilList;
import com.intellij.spring.impl.ide.model.xml.util.UtilMap;
import com.intellij.spring.impl.ide.model.xml.util.UtilSet;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtensionImpl
public class UtilSchemaInspection extends InjectionValueTypeInspection {
    @Override
    public void checkFileElement(DomFileElement<Beans> domFileElement, DomElementAnnotationHolder holder, Object state) {
        Beans beans = domFileElement.getRootElement();

        for (UtilSet springSet : DomUtil.getDefinedChildrenOfType(beans, UtilSet.class)) {
            checkSetBean(springSet, holder);
            checkElementsHolder(springSet, holder);
        }
        for (UtilList list : DomUtil.getDefinedChildrenOfType(beans, UtilList.class)) {
            checkListBean(list, holder);
            checkElementsHolder(list, holder);
        }
        for (UtilMap map : DomUtil.getDefinedChildrenOfType(beans, UtilMap.class)) {
            checkMapBean(map, holder);
        }
    }

    private void checkElementsHolder(ListOrSet springSet, DomElementAnnotationHolder holder) {
        checkSpringPropertyCollection(springSet, holder);
    }

    private static void checkMapBean(UtilMap map, DomElementAnnotationHolder holder) {
        checkProperClass(map.getMapClass(), Map.class, holder);
    }

    private static void checkListBean(UtilList list, DomElementAnnotationHolder holder) {
        checkProperClass(list.getListClass(), List.class, holder);
    }

    private static void checkSetBean(UtilSet set, DomElementAnnotationHolder holder) {
        checkProperClass(set.getSetClass(), Set.class, holder);
    }

    private static void checkProperClass(
        GenericAttributeValue<PsiClass> attrClass,
        Class aClass,
        DomElementAnnotationHolder holder
    ) {
        PsiClass psiClass = attrClass.getValue();
        if (psiClass != null) {
            if (!isAssignable(psiClass, aClass)) {
                LocalizeValue s = SpringLocalize.utilRequredClassMessage(aClass.getName());
                holder.createProblem(attrClass, s.get());
            }
        }
    }

    private static boolean isAssignable(PsiClass psiClass, Class fromClass) {
        Project project = psiClass.getProject();
        PsiType fromType = SpringConverterUtil.findType(fromClass, project);
        PsiClassType classType = JavaPsiFacade.getInstance(project).getElementFactory().createType(psiClass);

        return fromType != null && fromType.isAssignableFrom(classType);
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.utilSchemaInspectionName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "UtilSchemaInspection";
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}

