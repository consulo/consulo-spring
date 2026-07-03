package com.intellij.spring.impl.ide.model.actions.patterns.aop;

import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import consulo.aop.icon.AopIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class AopPatternsGroup extends DefaultActionGroup {

    public AopPatternsGroup() {
        super(SpringLocalize.springPatternsAopGroupName(), SpringLocalize.springPatternsAopGroupName(), AopIconGroup.gutterIntroduction());

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsAopAutoProxyCreator().get(),
            "aop-auto-proxy-creator"),
            AopIconGroup.gutterIntroduction()));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsAopAutoProxy().get(),
            "aop-auto-proxy"),
            AopIconGroup.gutterIntroduction()));

        setPopup(true);
    }
}