package com.intellij.spring.impl.ide.usages;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.Project;
import consulo.usage.rule.FileStructureGroupRuleProvider;
import consulo.usage.rule.UsageGroupingRule;

import jakarta.annotation.Nullable;

@ExtensionImpl
public class SpringBeanGroupRuleProvider implements FileStructureGroupRuleProvider {

  @Nullable
  public UsageGroupingRule getUsageGroupingRule(Project project) {
    return new SpringBeansGroupingRule();
  }
}
