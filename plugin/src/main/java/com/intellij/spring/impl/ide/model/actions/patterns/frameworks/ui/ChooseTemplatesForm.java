package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;

import consulo.disposer.Disposable;
import consulo.java.ex.facet.LibrariesValidationComponent;
import consulo.platform.Platform;
import consulo.ui.ex.awt.HyperlinkLabel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChooseTemplatesForm implements Disposable
{
  private JPanel myChoosePanel;
  private JPanel myTableViewPanel;
  private JPanel myLibsPanel;
  private final List<TemplateInfo> myTemplateInfos;
  private final LibrariesInfo myLibInfo;
  private LibrariesValidationComponent myLibrariesValidationComponent;
  private static final String JAVADOC = "Javadoc";
  private static final String DETAILS = "Details";

  public ChooseTemplatesForm(List<TemplateInfo> templates, LibrariesInfo libInfo) {
    myLibInfo = libInfo;

    myTableViewPanel.setLayout(new GridLayout(templates.size(), 1));
    myTemplateInfos = templates;

    for (final TemplateInfo template : myTemplateInfos) {
      final JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

      final JCheckBox checkBox = new JCheckBox(template.getName().get());
      checkBox.setSelected(template.isAccepted());
      checkBox.addActionListener(e -> template.setAccepted(checkBox.isSelected()));

      checkBoxPanel.add(checkBox);
      if (template.getApiLink() != null || template.getReferenceLink() != null) {
        final JLabel comp = new JLabel("(");
        final int width = comp.getFontMetrics(comp.getFont()).stringWidth("(");
        final int height = comp.getFontMetrics(comp.getFont()).getHeight();
        comp.setPreferredSize(new Dimension(width, height));

        checkBoxPanel.add(comp);
        if (template.getApiLink() != null) {
          checkBoxPanel.add(getApiLink(template));

          if (template.getReferenceLink() != null) {
            checkBoxPanel.add(new JLabel(","));
          }
        }
        checkBoxPanel.add(getReferenceLink(template));
        checkBoxPanel.add(new JLabel(")"));
      }

      final JPanel comboPanelWrapper = new JPanel(new BorderLayout());
      comboPanelWrapper.add(checkBoxPanel, BorderLayout.WEST);

      myTableViewPanel.add(comboPanelWrapper);

      myLibrariesValidationComponent = null; //facetEditorsFactory.createLibrariesValidationComponent(myLibInfo.getLibs(), myLibInfo.getModule(), myLibInfo.getName());
      myLibsPanel.add(myLibrariesValidationComponent.getComponent(), BorderLayout.CENTER);
      myLibrariesValidationComponent.addValidityListener(isValid -> {
      });
      myLibrariesValidationComponent.validate();
    }
  }

  private static JComponent getApiLink(final TemplateInfo template) {
    if (template.getApiLink() == null) return new JLabel();

    final HyperlinkLabel hyperlinkLabel = new HyperlinkLabel(JAVADOC);

    reduceHyperlinkFontSize(hyperlinkLabel);

    hyperlinkLabel.addHyperlinkListener(e -> Platform.current().openInBrowser(template.getApiLink()));
    return hyperlinkLabel;
  }

  private static void reduceHyperlinkFontSize(final HyperlinkLabel hyperlinkLabel) {
    final Font font = hyperlinkLabel.getFont();
    hyperlinkLabel.setFont(font.deriveFont((float)(font.getSize() - 1)));
  }

  private static JComponent getReferenceLink(final TemplateInfo template) {
    if (template.getReferenceLink() == null) return new JLabel();

    final HyperlinkLabel hyperlinkLabel = new HyperlinkLabel(DETAILS);

    reduceHyperlinkFontSize(hyperlinkLabel);

    hyperlinkLabel.addHyperlinkListener(e -> Platform.current().openInBrowser(template.getReferenceLink()));
    return hyperlinkLabel;
  }

  @Override
  public void dispose() {
  }

  public List<TemplateInfo> getTemplateInfos() {
    return myTemplateInfos;
  }

  public JComponent getComponent() {
    return myChoosePanel;
  }

  public void setLibrariesExist(final boolean valid) {
  }

  public LibrariesValidationComponent getLibrariesValidationComponent() {
    return myLibrariesValidationComponent;
  }
}
