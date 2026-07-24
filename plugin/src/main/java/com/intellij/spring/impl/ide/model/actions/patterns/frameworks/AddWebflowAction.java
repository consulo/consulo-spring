package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import consulo.java.ex.facet.LibraryInfo;
import consulo.language.editor.template.TemplateSettings;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public class AddWebflowAction extends AbstractFrameworkIntegrationAction {
    private static final String WEBFLOW_STRING_ID = "webflow";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{"org.springframework.webflow.definition.registry.FlowDefinitionRegistry"};
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos(WEBFLOW_STRING_ID);

        return new LibrariesInfo(libraryInfos, module, WEBFLOW_STRING_ID);
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo flowRegistry = new TemplateInfo(
            module,
            settings.getTemplateById("flow-registry"),
            SpringLocalize.springPatternsWebflowRegistry()
        );

        TemplateInfo flowExecutor = new TemplateInfo(
            module,
            settings.getTemplateById("flow-executor"),
            SpringLocalize.springPatternsWebflowExecutor()
        );

        TemplateInfo flowBuilderServices = new TemplateInfo(
            module,
            settings.getTemplateById("flow-builder-serices"),
            SpringLocalize.springPatternsWebflowBuilderServices()
        );

        TemplateInfo conversationService = new TemplateInfo(
            module,
            settings.getTemplateById("conversation-service"),
            SpringLocalize.springPatternsWebflowServicesConversionService(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo expressionParser = new TemplateInfo(
            module,
            settings.getTemplateById("expression-parser"),
            SpringLocalize.springPatternsWebflowServicesExpressionParser(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo factoryCreator = new TemplateInfo(
            module,
            settings.getTemplateById("factory-creator"),
            SpringLocalize.springPatternsWebflowServicesViewFactoryCreator(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo formatterRegistry = new TemplateInfo(
            module,
            settings.getTemplateById("formatter-registry"),
            SpringLocalize.springPatternsWebflowServicesViewFormatterRegistry(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo exeListener = new TemplateInfo(
            module,
            settings.getTemplateById("flow-execution-listener"),
            SpringLocalize.springPatternsWebflowExecutionListener(),
            null,
            false
        );

        infos.add(flowBuilderServices);
        infos.add(flowRegistry);
        infos.add(exeListener);
        infos.add(flowExecutor);

        infos.add(conversationService);
        infos.add(expressionParser);
        infos.add(factoryCreator);
        infos.add(formatterRegistry);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsWebflowGroupName();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return SpringIcons.SPRING_ICON;
    }
}