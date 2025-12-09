package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.CollectionElements;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringElementsHolder;
import com.intellij.spring.impl.model.beans.SpringBeanImpl;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.refactoring.inline.InlineHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.usage.UsageInfo;
import consulo.util.collection.MultiMap;
import consulo.xml.lang.xml.XMLLanguage;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.DomManager;
import consulo.xml.util.xml.GenericAttributeValue;
import consulo.xml.util.xml.GenericDomValue;
import consulo.xml.util.xml.reflect.DomCollectionChildDescription;
import consulo.xml.util.xml.reflect.DomFixedChildDescription;
import consulo.xml.util.xml.reflect.DomGenericInfo;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringInlineHandler implements InlineHandler {
    private final static Logger LOG = Logger.getInstance(SpringInlineHandler.class);
    private static final String PARENT_ATTR = "parent";

    @Override
    public Settings prepareInlineElement(@Nonnull PsiElement element, Editor editor, boolean invokedOnReference) {
        return () -> false;
    }

    @Override
    @RequiredWriteAction
    public void removeDefinition(@Nonnull PsiElement element, @Nonnull Settings settings) {
        DomElement domElement;
        if (element instanceof XmlTag) {
            domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag) element);
            if (domElement != null) {
                domElement.undefine();
            }
        }
    }

    @Override
    public Inliner createInliner(@Nonnull PsiElement element, @Nonnull Settings settings) {
        if (!(element instanceof XmlTag)) {
            return null;
        }
        return new Inliner() {
            @Override
            @RequiredReadAction
            public MultiMap<PsiElement, LocalizeValue> getConflicts(@Nonnull PsiReference reference, @Nonnull PsiElement referenced) {
                return null;
            }

            @Override
            @RequiredWriteAction
            public void inlineUsage(@Nonnull UsageInfo usage, @Nonnull PsiElement referenced) {
                if (!(referenced instanceof XmlTag)) {
                    return;
                }
                Project project = referenced.getProject();
                DomManager domManager = DomManager.getDomManager(project);
                DomSpringBean bean = (DomSpringBean) domManager.getDomElement((XmlTag) referenced);

                if (usage.getElement() instanceof XmlAttributeValue attrValue) {
                    XmlAttribute attribute = (XmlAttribute) attrValue.getParent();
                    GenericAttributeValue value = domManager.getDomElement(attribute);
                    assert value != null;
                    DomElement parent = value.getParent();
                    assert parent != null;
                    if (parent instanceof SpringBean thisBean) {
                        if (attribute.getName().equals(PARENT_ATTR)) {
                            mergeValue(thisBean, thisBean.getScope());
                            mergeValue(thisBean, thisBean.getAbstract());
                            mergeValue(thisBean, thisBean.getLazyInit());

                            mergeValue(thisBean, thisBean.getAutowireCandidate());
                            mergeValue(thisBean, thisBean.getAutowire());
                            mergeValue(thisBean, thisBean.getDependencyCheck());
                            mergeValue(thisBean, thisBean.getDependsOn());

                            mergeValue(thisBean, thisBean.getFactoryBean());
                            mergeValue(thisBean, thisBean.getFactoryMethod());
                            mergeValue(thisBean, thisBean.getInitMethod());
                            mergeValue(thisBean, thisBean.getDestroyMethod());

                            mergeValue(thisBean, thisBean.getDescription());

                            mergeList(thisBean, SpringBeanImpl.CTOR_ARGS_GETTER, springBean -> springBean.addConstructorArg());
                            mergeList(thisBean, SpringBeanImpl.PROPERTIES_GETTER, springBean -> springBean.addProperty());
                            mergeList(
                                thisBean,
                                springBean -> springBean.getReplacedMethods(),
                                springBean -> springBean.addReplacedMethod()
                            );
                            value.undefine();
                            reformat(parent);
                        }
                    }
                    else if (parent instanceof SpringElementsHolder) {
                        copyBean(bean, parent);
                        value.undefine();
                        reformat(parent);
                    }
                    else {
                        DomElement grandParent = parent.getParent();
                        if (grandParent instanceof SpringElementsHolder) {
                            copyBean(bean, grandParent);
                            parent.undefine();
                            reformat(grandParent);
                        }
                        else if (grandParent instanceof CollectionElements) {
                            copyBean(bean, grandParent);
                            parent.undefine();
                            reformat(grandParent);
                        }
                        else {
                            LOG.error("Cannot inline " + attribute);
                        }
                    }
                }
            }
        };
    }

    private static <T extends GenericDomValue<?>> void mergeValue(SpringBean springBean, T value) {
        T mergedValue = SpringUtils.getMergedValue(springBean, value);
        if (mergedValue != value) {
            value.setStringValue(mergedValue.getStringValue());
        }
    }

    public static <T extends DomElement> void mergeList(
        SpringBean springBean,
        Function<SpringBean, Collection<T>> getter,
        Function<SpringBean, T> adder
    ) {
        Set<T> merged = SpringUtils.getMergedSet(springBean, getter);
        Collection<T> existing = getter.apply(springBean);
        for (T t : existing) {
            if (!merged.contains(t)) {
                t.undefine();
            }
            else {
                merged.remove(t);
            }
        }
        for (T t : merged) {
            T newElement = adder.apply(springBean);
            newElement.copyFrom(t);
        }
    }

    private static void reformat(DomElement domElement) {
        try {
            CodeStyleManager.getInstance(domElement.getManager().getProject()).reformat(domElement.getXmlTag());
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }

    private static void copyBean(DomSpringBean from, DomElement parent) {
        DomCollectionChildDescription description = (DomCollectionChildDescription) from.getChildDescription();
        DomGenericInfo info = parent.getGenericInfo();
        String name = description.getXmlElementName();
        String namespaceKey = description.getXmlName().getNamespaceKey();
        DomSpringBean to;
        DomCollectionChildDescription targetDescription = info.getCollectionChildDescription(name, namespaceKey);
        if (targetDescription != null) {
            to = (DomSpringBean) targetDescription.addValue(parent);
        }
        else {
            DomFixedChildDescription fixedDescr = info.getFixedChildDescription(name, namespaceKey);
            assert fixedDescr != null;
            to = (DomSpringBean) fixedDescr.getValues(parent).get(0);
        }

        to.copyFrom(from);
        to.getId().undefine();
        if (to instanceof SpringBean springBean) {
            springBean.getName().undefine();
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return XMLLanguage.INSTANCE;
    }
}
