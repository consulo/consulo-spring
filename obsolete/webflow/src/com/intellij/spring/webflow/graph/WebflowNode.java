package com.intellij.spring.webflow.graph;

import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface WebflowNode<T extends DomElement> {
  @Nullable
  String getName();

  @NotNull
  WebflowNodeType getNodeType();

  Icon getIcon();

  @NotNull
  T getIdentifyingElement();

  List<WebflowNamedAction> getAllNodeActions();
}