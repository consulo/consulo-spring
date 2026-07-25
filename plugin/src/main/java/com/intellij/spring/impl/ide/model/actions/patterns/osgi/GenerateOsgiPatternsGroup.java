package com.intellij.spring.impl.ide.model.actions.patterns.osgi;

import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateOsgiPatternsGroup extends DefaultActionGroup {
    public GenerateOsgiPatternsGroup() {
        super(SpringLocalize.springPatternsOsgiGroupName(), SpringLocalize.springPatternsOsgiGroupName(), PatternIcons.FACTORY_BEAN_ICON);
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiService(), "osgi_simple_service"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiMultipleService(), "osgi_multiple_service"),
            SpringImplIconGroup.springbean()
        ));
        addSeparator();
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRef(), "osgi_ref"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiMultipleRef(), "osgi_multi_ref"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRefWithListener(), "osgi_ref_listener"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRefWithBean(), "osgi_ref_with_bean"),
            SpringImplIconGroup.springbean()
        ));

        addSeparator();

        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiList(), "osgi_list"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiListComparator(), "osgi_list_comparator"),
            SpringImplIconGroup.springbean()
        ));

        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiSet(), "osgi_set"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiSetComparator(), "osgi_set_comparator"),
            SpringImplIconGroup.springbean()
        ));

        setPopup(true);
    }
}
