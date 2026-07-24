package com.intellij.spring.impl.ide.model.values;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesFilesManager;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.lang.properties.references.CreatePropertyFix;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.converters.ResourceResolverUtils;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import com.intellij.spring.impl.ide.model.xml.context.PropertyPlaceholder;
import com.intellij.spring.impl.ide.model.xml.util.UtilProperties;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.LocalQuickFixProvider;
import consulo.language.psi.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.content.ProjectRootManager;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Condition;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.GenericDomValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class PlaceholderUtils {
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    public static final String PLACEHOLDER_PREFIX_PROPERTY_NAME = "placeholderPrefix";
    public static final String PLACEHOLDER_SUFFIX_PROPERTY_NAME = "placeholderSuffix";

    public static final String PLACEHOLDER_CONFIGURER_CLASS = "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer";

    private static final String LOCATION_PROPERTY_NAME = "location";
    private static final String LOCATIONS_PROPERTY_NAME = "locations";
    private static final String PROPERTIES_PROPERTY_NAME = "properties";
    private static final String PROPERTIES_ARRAY_PROPERTY_NAME = "propertiesArray";

    private static final Key<CachedValue<Pair<String, String>>> PLACEHOLDER_PREFIX_SUFFIX = Key.create("PLACEHOLDER_PREFIX_SUFFIX");

    private PlaceholderUtils() {
    }

    public static List<SpringBaseBeanPointer> getPlaceholderConfigurerBeans(DomElement domElement) {
        Project project = domElement.getManager().getProject();
        PsiClass placeholderClass =
            JavaPsiFacade.getInstance(project).findClass(PLACEHOLDER_CONFIGURER_CLASS, GlobalSearchScope.allScope(project));

        if (placeholderClass == null) {
            return Collections.emptyList();
        }

        SpringModel model = SpringConverterUtil.getSpringModel(domElement);
        if (model == null) {
            return Collections.emptyList();
        }

        List<SpringBaseBeanPointer> configurers = model.findBeansByPsiClassWithInheritance(placeholderClass);

        return configurers == null ? Collections.<SpringBaseBeanPointer>emptyList() : configurers;
    }

    public static List<PropertiesFile> getResources(CommonSpringBean configurerBean) {
        List<PropertiesFile> resources = new ArrayList<PropertiesFile>();
        List<Pair<String, PsiElement>> locations = getLocations(configurerBean);

        boolean hasNotResolvedLocations = false;

        for (Pair<String, PsiElement> location : locations) {
            Set<PropertiesFile> propertiesFiles = getPropertiesFile(location);
            if (propertiesFiles != null && !propertiesFiles.isEmpty()) {
                resources.addAll(propertiesFiles);
            }
            else {
                hasNotResolvedLocations = true;
            }
        }
        if (locations.size() > 0 && hasNotResolvedLocations) {
            //some locations were not resolved, so we'll try load all properties files(IDEADEV-16888)
            PsiManager psiManager = configurerBean.getPsiManager();
            PropertiesFilesManager.getInstance(psiManager.getProject()).processAllPropertiesFiles((s, propertiesFile) -> {
                if (!resources.contains(propertiesFile)) {
                    resources.add(propertiesFile);
                }
                return true;
            });
        }
        return resources;
    }

    @Nullable
    private static Set<PropertiesFile> getPropertiesFile(Pair<String, PsiElement> location) {
        return ResourceResolverUtils
            .addResourceFilesFrom(location.second, location.first, ",", new HashSet<PropertiesFile>(), new Condition<PsiFileSystemItem>() {
                @Override
                public boolean value(PsiFileSystemItem item) {
                    return item instanceof PropertiesFile;
                }
            });
    }

    private static List<Pair<String, PsiElement>> getLocations(CommonSpringBean configurerBean) {
        List<Pair<String, PsiElement>> locations = new ArrayList<Pair<String, PsiElement>>();

        if (configurerBean instanceof PropertyPlaceholder) {
            GenericAttributeValue<String> location = ((PropertyPlaceholder) configurerBean).getLocation();
            if (!StringUtil.isEmptyOrSpaces(location.getStringValue())) {
                locations.add(new Pair<String, PsiElement>(location.getStringValue(), location.getXmlElement()));
            }
        }
        else {
            SpringPropertyDefinition locationProperty = SpringUtils.findPropertyByName(configurerBean, LOCATION_PROPERTY_NAME);
            if (locationProperty != null) {
                Pair<String, PsiElement> value = SpringUtils.getPropertyValue(locationProperty);
                if (value != null) {
                    locations.add(value);
                }
            }
            SpringPropertyDefinition locationsProperty = SpringUtils.findPropertyByName(configurerBean, LOCATIONS_PROPERTY_NAME);
            if (locationsProperty instanceof SpringProperty) {
                Pair<String, PsiElement> propertyValue = SpringUtils.getPropertyValue(locationsProperty);
                if (propertyValue != null) {
                    locations.add(propertyValue);
                }
                else {
                    ListOrSet list = ((SpringProperty) locationsProperty).getList();

                    for (SpringValue value : list.getValues()) {
                        if (!StringUtil.isEmptyOrSpaces(value.getStringValue())) {
                            locations.add(new Pair<String, PsiElement>(value.getStringValue(), value.getXmlElement()));
                        }
                    }
                    ListOrSet set = ((SpringProperty) locationsProperty).getSet();
                    for (SpringValue value : set.getValues()) {
                        if (!StringUtil.isEmptyOrSpaces(value.getStringValue())) {
                            locations.add(new Pair<String, PsiElement>(value.getStringValue(), value.getXmlElement()));
                        }
                    }
                }
            }
        }
        SpringPropertyDefinition propertiesProperty = SpringUtils.findPropertyByName(configurerBean, PROPERTIES_PROPERTY_NAME);
        if (propertiesProperty != null) {
            GenericDomValue<SpringBeanPointer> element = propertiesProperty.getRefElement();
            if (element != null) {
                addLocations(locations, element.getValue());
            }
        }

        SpringPropertyDefinition propertiesArrayProperty = SpringUtils.findPropertyByName(configurerBean, PROPERTIES_ARRAY_PROPERTY_NAME);
        if (propertiesArrayProperty != null) {
            GenericDomValue<SpringBeanPointer> element = propertiesArrayProperty.getRefElement();
            if (element != null) {
                addLocations(locations, element.getValue());
            }
            if (propertiesArrayProperty instanceof SpringProperty) {
                ListOrSet list = ((SpringProperty) propertiesArrayProperty).getList();
                for (SpringRef value : list.getRefs()) {
                    addLocations(locations, value.getBean().getValue());
                    addLocations(locations, value.getLocal().getValue());
                }

                ListOrSet set = ((SpringProperty) propertiesArrayProperty).getSet();
                for (SpringRef value : set.getRefs()) {
                    addLocations(locations, value.getBean().getValue());
                    addLocations(locations, value.getLocal().getValue());
                }
            }
        }

        return locations;
    }

    private static void addLocations(List<Pair<String, PsiElement>> locations, SpringBeanPointer beanPointer) {
        if (beanPointer != null) {
            CommonSpringBean springBean = beanPointer.getSpringBean();
            GenericDomValue<String> location = getLocationDomElementValue(springBean);

            if (location != null && !StringUtil.isEmptyOrSpaces(location.getStringValue())) {
                locations.add(new Pair<String, PsiElement>(location.getStringValue(), DomUtil.getValueElement(location)));
            }
        }
    }

    @Nullable
    private static GenericDomValue<String> getLocationDomElementValue(CommonSpringBean springBean) {
        if (springBean instanceof UtilProperties) {
            UtilProperties utilProperties = (UtilProperties) springBean;
            return utilProperties.getLocation();
        }
        if (springBean instanceof SpringBean) {
            // location for org.springframework.beans.factory.config.PropertiesFactoryBean
            PsiClass psiClass = springBean.getBeanClass();
            if (psiClass != null && UtilProperties.BEAN_CLASS_NAME.equals(psiClass.getQualifiedName())) {
                SpringPropertyDefinition location = SpringUtils.findPropertyByName(springBean, LOCATION_PROPERTY_NAME);
                if (location != null) {
                    return (GenericDomValue<String>) location.getValueElement();
                }
            }
        }
        return null;
    }

    public static boolean isPlaceholder(GenericDomValue genericDomValue) {

        String stringValue = genericDomValue.getStringValue();

        if (stringValue != null && !StringUtil.isEmptyOrSpaces(stringValue)) {
            List<SpringBaseBeanPointer> configurers = getPlaceholderConfigurerBeans(genericDomValue);

            return isPlaceholder(stringValue, configurers);
        }
        return false;
    }

    public static boolean isPlaceholder(String stringValue, List<SpringBaseBeanPointer> configurers) {
        for (SpringBaseBeanPointer configurer : configurers) {
            CommonSpringBean bean = configurer.getSpringBean();
            if (bean instanceof DomSpringBean) {
                Pair<String, String> prefixAndSuffix = getPlaceholderPrefixAndSuffix((DomSpringBean) bean);
                int prefixPos = stringValue.indexOf(prefixAndSuffix.first);
                if (prefixPos >= 0 && prefixPos < stringValue.indexOf(prefixAndSuffix.second)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Pair<String, String> getPlaceholderPrefixAndSuffix(final DomSpringBean placeholderBean) {
        CachedValue<Pair<String, String>> cachedValue = placeholderBean.getUserData(PLACEHOLDER_PREFIX_SUFFIX);
        if (cachedValue == null) {
            cachedValue =
                CachedValuesManager.getManager(placeholderBean.getPsiManager().getProject())
                    .createCachedValue(new CachedValueProvider<Pair<String, String>>() {
                        @Override
                        public Result<Pair<String, String>> compute() {
                            return new Result<Pair<String, String>>(getPlaceholderPrefixAndSuffixInner(placeholderBean),
                                placeholderBean.getXmlElement());
                        }
                    }, false);
            placeholderBean.putUserData(PLACEHOLDER_PREFIX_SUFFIX, cachedValue);
        }
        return cachedValue.getValue();
    }

    public static Pair<String, String> getPlaceholderPrefixAndSuffixInner(DomSpringBean placeholderBean) {
        String prefix = DEFAULT_PLACEHOLDER_PREFIX;
        String suffix = DEFAULT_PLACEHOLDER_SUFFIX;

        SpringPropertyDefinition userDefinedPrefixProperty =
            SpringUtils.findPropertyByName(placeholderBean, PLACEHOLDER_PREFIX_PROPERTY_NAME);
        if (userDefinedPrefixProperty != null) {
            String value = SpringUtils.getStringPropertyValue(userDefinedPrefixProperty);
            if (!StringUtil.isEmptyOrSpaces(value)) {
                prefix = value;
            }
        }

        SpringPropertyDefinition userDefinedSuffixProperty =
            SpringUtils.findPropertyByName(placeholderBean, PLACEHOLDER_SUFFIX_PROPERTY_NAME);
        if (userDefinedSuffixProperty != null) {
            String value = SpringUtils.getStringPropertyValue(userDefinedSuffixProperty);
            if (!StringUtil.isEmptyOrSpaces(value)) {
                suffix = value;
            }
        }

        return new Pair<String, String>(prefix, suffix);
    }

    @Nonnull
    public static PsiReference[] createPlaceholderPropertiesReferences(GenericDomValue genericDomValue) {
        Map<TextRange, Info> textRanges = getTextRanges(genericDomValue);
        if (textRanges.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }

        XmlElement valueElement = DomUtil.getValueElement(genericDomValue);
        List<PsiReference> references = new ArrayList<PsiReference>();
        for (TextRange textRange : textRanges.keySet()) {
            Info info = textRanges.get(textRange);
            references.add(createPropertyReference(valueElement, textRange, info.text, info.placeholders));
        }
        return references.toArray(new PsiReference[references.size()]);
    }

    private static PropertyReference createPropertyReference(PsiElement element,
                                                             TextRange textRange,
                                                             String value,
                                                             List<SpringBaseBeanPointer> placeholders) {
        return new PlaceholderPropertyReference(element, textRange, value, placeholders);
    }

    private static Map<TextRange, Info> getTextRanges(GenericDomValue domValue) {
        List<SpringBaseBeanPointer> configurerBeans = getPlaceholderConfigurerBeans(domValue);
        if (configurerBeans.size() > 0) {
            PsiElement psiElement = DomUtil.getValueElement(domValue);
            return getTextRanges(configurerBeans, psiElement);
        }
        return Collections.emptyMap();
    }

    private static Map<TextRange, Info> getTextRanges(List<SpringBaseBeanPointer> configurers, PsiElement element) {
        Map<TextRange, Info> textRanges = new HashMap<TextRange, Info>();
        for (SpringBaseBeanPointer configurer : configurers) {
            CommonSpringBean bean = configurer.getSpringBean();
            if (!(bean instanceof DomSpringBean)) {
                continue;
            }

            String text = element.getText();
            Pair<String, String> prefixAndSuffix = getPlaceholderPrefixAndSuffix((DomSpringBean) bean);
            int prefixIndex = text.indexOf(prefixAndSuffix.first);
            int suffixIndex = text.indexOf(prefixAndSuffix.second);
            while (prefixIndex >= 0 && prefixIndex < suffixIndex) {
                int offset = prefixIndex + prefixAndSuffix.first.length();
                int length = suffixIndex - prefixIndex - prefixAndSuffix.first.length();

                TextRange textRange = TextRange.from(offset, length);
                List<SpringBaseBeanPointer> placeholders =
                    textRanges.get(textRange) != null ? textRanges.get(textRange).placeholders : new ArrayList<SpringBaseBeanPointer>();
                placeholders.add(configurer);

                textRanges.put(textRange, new Info(text.substring(offset, offset + length), placeholders,
                    new TextRange(prefixIndex, suffixIndex + prefixAndSuffix.second.length())));

                if (suffixIndex + 1 >= text.length()) {
                    break;
                }

                prefixIndex = text.indexOf(prefixAndSuffix.first, suffixIndex + 1);
                suffixIndex = text.indexOf(prefixAndSuffix.second, suffixIndex + 1);
            }
        }
        return textRanges;
    }

    private static class Info {
        final String text;
        final List<SpringBaseBeanPointer> placeholders;
        final TextRange fullTextRange;

        public Info(String text, List<SpringBaseBeanPointer> placeholders, TextRange fullTextRange) {
            this.text = text;
            this.placeholders = placeholders;
            this.fullTextRange = fullTextRange;
        }
    }

    private static class PlaceholderPropertyReference extends PropertyReference implements LocalQuickFixProvider {
        private final String myKey;
        private final List<SpringBaseBeanPointer> myPlaceholders;

        public PlaceholderPropertyReference(@Nonnull PsiElement psiElement,
                                            @Nonnull TextRange textRange,
                                            @Nonnull String key,
                                            List<SpringBaseBeanPointer> placeholders) {
            super(key, psiElement, null, true, textRange);

            myKey = key;
            myPlaceholders = placeholders;
        }

        protected void addVariantsFromFile(PropertiesFile propertiesFile, Set<Object> variants) {
            if (propertiesFile == null) return;
            if (!ProjectRootManager.getInstance(myElement.getProject()).getFileIndex().isInContent(propertiesFile.getVirtualFile())) return;
            List<? extends IProperty> properties = propertiesFile.getProperties();
            for (IProperty property : properties) {
                addKey(property, variants);
            }
        }

        @Override
        public Object[] getVariants() {
            Set<Object> variants = new HashSet<Object>();
            for (SpringBaseBeanPointer placeholder : myPlaceholders) {
                for (PropertiesFile propertiesFile : getResources(placeholder.getSpringBean())) {
                    addVariantsFromFile(propertiesFile, variants);
                }
            }

            for (SpringBaseBeanPointer placeholder : myPlaceholders) {
                CommonSpringBean placeholderBean = placeholder.getSpringBean();
                SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(placeholderBean, "properties");
                if (propertyDefinition instanceof SpringProperty) {
                    for (Prop prop : ((SpringProperty) propertyDefinition).getProps().getProps()) {
                        String keyValue = prop.getKey().getStringValue();
                        if (!StringUtil.isEmptyOrSpaces(keyValue)) {
                            variants.add(keyValue);
                        }
                    }

                    for (Object str : getValueProperties((SpringProperty) propertyDefinition).keySet()) {
                        if (!StringUtil.isEmptyOrSpaces((String) str)) {
                            variants.add(str);
                        }
                    }
                }
            }

            return ArrayUtil.toObjectArray(variants);
        }

        @Nonnull
        private static Properties getValueProperties(SpringProperty property) {
            Properties props = new Properties();
            String value = property.getValue().getStringValue();
            if (!StringUtil.isEmptyOrSpaces(value)) {
                try {
                    props.load(new ByteArrayInputStream(value.getBytes()));
                }
                catch (IOException ignored) {
                }
            }
            return props;
        }

        @Override
        @Nonnull
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            Set<IProperty> properties = new HashSet<IProperty>();
            for (SpringBaseBeanPointer placeholder : myPlaceholders) {
                for (PropertiesFile resource : getResources(placeholder.getSpringBean())) {
                    properties.addAll(resource.findPropertiesByKey(myKey));
                }
            }

            Set<DomElement> configurerProperties = getPlaceholderConfigurerProperties(myKey, myPlaceholders);  // IDEADEV-29790 && IDEADEV-31411

            ResolveResult[] result = new ResolveResult[properties.size() + configurerProperties.size()];
            if (properties.size() > 0 || configurerProperties.size() > 0) {
                int i = 0;
                for (IProperty property : properties) {
                    result[i++] = new PsiElementResolveResult(property.getPsiElement());
                }
                for (DomElement configurerProperty : configurerProperties) {
                    result[i++] = new PsiElementResolveResult(configurerProperty.getXmlElement());
                }
            }
            else if (Platform.current().jvm().getRuntimeProperty(myKey) != null) {
                return new ResolveResult[]{new PsiElementResolveResult(getElement())};
            }
            return result;
        }

        @Nonnull
        private static Set<DomElement> getPlaceholderConfigurerProperties(@Nonnull String key,
                                                                          List<SpringBaseBeanPointer> placeholders) {
            Set<DomElement> proprs = new HashSet<DomElement>();
            for (SpringBaseBeanPointer placeholder : placeholders) {
                CommonSpringBean placeholderBean = placeholder.getSpringBean();
                SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(placeholderBean, "properties");
                if (propertyDefinition instanceof SpringProperty) {
                    for (Prop prop : ((SpringProperty) propertyDefinition).getProps().getProps()) {
                        if (key.equals(prop.getKey().getStringValue())) {
                            proprs.add(prop);
                        }
                    }
                    for (Object propName : getValueProperties((SpringProperty) propertyDefinition).keySet()) {
                        if (key.equals(propName)) {
                            proprs.add(((SpringProperty) propertyDefinition).getValue());
                            break;
                        }
                    }
                }
            }

            return proprs;
        }

        @Override
        public LocalQuickFix[] getQuickFixes() {
            List<PropertiesFile> propertiesFiles = new ArrayList<PropertiesFile>();
            for (SpringBaseBeanPointer placeholder : myPlaceholders) {
                propertiesFiles.addAll(getResources(placeholder.getSpringBean()));

            }
            CreatePropertyFix fix = new CreatePropertyFix(getElement(), myKey, propertiesFiles);

            return new LocalQuickFix[]{fix};
        }
    }

    @RequiredReadAction
    public static Collection<String> getExpandedVariants(GenericDomValue value) {
        String stringValue = value.getStringValue();
        Map<TextRange, Info> textRanges = getTextRanges(value);
        if (textRanges.isEmpty()) {
            return ContainerUtil.createMaybeSingletonList(stringValue);
        }

        TextRange[] ranges = textRanges.keySet().toArray(new TextRange[textRanges.size()]);
        Arrays.sort(ranges, (o1, o2) -> o1.getStartOffset() - o2.getStartOffset());
        XmlElement valueElement = DomUtil.getValueElement(value);
        assert valueElement != null;
        HashSet<String> result = new HashSet<String>();
        ResolveResult[][] variants = new ResolveResult[ranges.length][];
        for (int i = 0; i < ranges.length; i++) {
            TextRange range = ranges[i];
            Info info = textRanges.get(range);
            PropertyReference reference = createPropertyReference(valueElement, range, info.text, info.placeholders);
            ResolveResult[] results = reference.multiResolve(false);
            variants[i] = results;
        }
        int[] indices = new int[ranges.length];
        ElementManipulator<XmlElement> manipulator = ElementManipulators.getManipulator(valueElement);
        assert manipulator != null;
        int offsetBase = -manipulator.getRangeInElement(valueElement).getStartOffset();
        while (true) {
            int offset = offsetBase;
            StringBuilder sb = new StringBuilder(stringValue);
            for (int i = 0; i < indices.length; i++) {
                Info info = textRanges.get(ranges[i]);
                if (variants[i].length == 0) {
                    continue;
                }
                ResolveResult resolveResult = variants[i][indices[i]];
                PsiElement element = resolveResult.getElement();
                if (!(element instanceof Property)) {
                    continue;
                }
                Property property = (Property) element;
                String replacement = property.getValue();
                if (replacement == null) {
                    continue;
                }
                sb.replace(offset + info.fullTextRange.getStartOffset(), offset + info.fullTextRange.getEndOffset(), replacement);
                offset += replacement.length() - info.fullTextRange.getLength();
            }
            result.add(sb.toString());

            boolean quit = true;
            for (int idx = 0; idx < indices.length; idx++) {
                if (indices[idx] < variants[idx].length - 1) {
                    quit = false;
                    indices[idx]++;
                    for (int i = 0; i < idx; i++) {
                        indices[i] = 0;
                    }
                    break;
                }
            }
            if (quit) {
                break;
            }

        }
        return result;
    }
}
