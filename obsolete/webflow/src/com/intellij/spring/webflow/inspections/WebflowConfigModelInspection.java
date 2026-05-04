package com.intellij.spring.webflow.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.spring.model.highlighting.SpringBeanInspectionBase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.webflow.config.model.xml.version1_0.Executor;
import com.intellij.spring.webflow.config.model.xml.version1_0.Listener;
import com.intellij.spring.webflow.config.model.xml.version1_0.Registry;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class WebflowConfigModelInspection extends SpringBeanInspectionBase {

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final Beans beans = domFileElement.getRootElement();

    for (Executor executor : DomUtil.getDefinedChildrenOfType(beans, Executor.class)) {
      WebflowUtil.checkBeanOfSpecificType(executor.getRegistryRef(), Registry.FLOW_DEFINITION_REGISTRY_CLASS, holder);
      WebflowUtil.checkBeanOfSpecificType(executor.getRepository().getConversationManagerRef(), WebflowConstants.CONVERSATION_MANAGER_CLASS_NAME, holder);

      for (Listener listener : executor.getExecutionListeners().getListeners()) {
        WebflowUtil.checkBeanOfSpecificType(listener.getRef(), WebflowConstants.EXECUTION_LISTENER_CLASS_NAME, holder);
      }
    } 
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return WebflowBundle.message("webflow.model.config.inspection.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "WebflowConfigModelInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}


