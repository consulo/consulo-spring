package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.converters.ConversationManagerRefConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow-config:repositoryType interface.
 */
public interface Repository extends WebflowConfigDomElement {

  /**
   * Returns the value of the type child.
   * <pre>
   * <h3>Attribute null:type documentation</h3>
   * The type of flow execution repository to use.  The repository is responsible for managing flow execution
   * persistence between requests.
   * </pre>
   *
   * @return the value of the type child.
   */
  @NotNull
  @Required
  GenericAttributeValue<RepositoryTypeAttribute> getType();


  /**
   * Returns the value of the max-conversations child.
   * <pre>
   * <h3>Attribute null:max-conversations documentation</h3>
   * The maximum number of concurrent conversations allowed by this repository.  It is illegal to set this
   * attribute if you are also setting the 'conversation-manager-ref' attribute.
   * </pre>
   *
   * @return the value of the max-conversations child.
   */
  @NotNull
  GenericAttributeValue<Integer> getMaxConversations();


  /**
   * Returns the value of the max-continuations child.
   * <pre>
   * <h3>Attribute null:max-continuations documentation</h3>
   * The maximum number of flow execution continuations (snapshots) allowed by this repository per conversation.
   * This attribute is only relevant when the repository type is 'continuation'.
   * </pre>
   *
   * @return the value of the max-continuations child.
   */
  @NotNull
  GenericAttributeValue<Integer> getMaxContinuations();

  /**
   * Returns the value of the conversation-manager-ref child.
   * <pre>
   * <h3>Attribute null:conversation-manager-ref documentation</h3>
   * The idref of the conversation manager this repository should use.  Setting this attribute
   * allows full control over the conversation manager implementation and configuration.
   * When used, any value for the 'max-conversations' attribute is ignored.
   * </pre>
   *
   * @return the value of the conversation-manager-ref child.
   */
  @NotNull
  @Convert(ConversationManagerRefConverter.class)
  GenericAttributeValue<SpringBeanPointer> getConversationManagerRef();
}
