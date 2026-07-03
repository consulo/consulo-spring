package com.intellij.spring.impl.ide.model.actions.patterns.osgi;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateOsgiPatternsGroup extends DefaultActionGroup {

    public GenerateOsgiPatternsGroup() {
        super(SpringLocalize.springPatternsOsgiGroupName(), SpringLocalize.springPatternsOsgiGroupName(), PatternIcons.FACTORY_BEAN_ICON);
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiService().get(), "osgi_simple_service"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiMultipleService().get(), "osgi_multiple_service"), SpringIcons.SPRING_BEAN_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRef().get(), "osgi_ref"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiMultipleRef().get(), "osgi_multi_ref"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRefWithListener().get(), "osgi_ref_listener"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiRefWithBean().get(), "osgi_ref_with_bean"), SpringIcons.SPRING_BEAN_ICON));

        addSeparator();

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiList().get(), "osgi_list"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiListComparator().get(), "osgi_list_comparator"), SpringIcons.SPRING_BEAN_ICON));

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiSet().get(), "osgi_set"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsOsgiSetComparator().get(), "osgi_set_comparator"), SpringIcons.SPRING_BEAN_ICON));

        setPopup(true);
    }
}
