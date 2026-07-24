/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide;

import com.intellij.spring.impl.ide.facet.SpringSchemaVersion;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.FileTemplateDescriptor;
import consulo.fileTemplate.FileTemplateGroupDescriptor;
import consulo.fileTemplate.FileTemplateGroupDescriptorFactory;

@ExtensionImpl
public class SpringBeansTemplatesFactory implements FileTemplateGroupDescriptorFactory {

  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(SpringBundle.message("spring"), SpringIcons.FILESET);

    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_1_DTD.getTemplateName(), SpringIcons.CONFIG_FILE));
    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_2_DTD.getTemplateName(), SpringIcons.CONFIG_FILE));
    group.addTemplate(new FileTemplateDescriptor(SpringSchemaVersion.Spring_2_Schema.getTemplateName(), SpringIcons.CONFIG_FILE));
    return group;
  }
}
