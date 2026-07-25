/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide;

import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.schemas.SpringSchemaProvider;
import com.intellij.spring.impl.model.CustomBeanWrapperImpl;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.util.Semaphore;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.ide.ServiceManager;
import consulo.java.ex.jsp.JspSpiUtil;
import consulo.java.execution.configurations.OwnJavaParameters;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.Key;
import consulo.util.io.ClassPathUtil;
import consulo.util.jdom.JDOMUtil;
import consulo.util.lang.ExceptionUtil;
import consulo.util.lang.StringEscapeUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.PathsList;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlElementFactory;
import consulo.xml.language.psi.XmlElementVisitor;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.jdom.Document;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author peter
 */
@Singleton
@State(name = "CustomBeanRegistry", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/customSpringBeans.xml"))
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class CustomBeanRegistry implements PersistentStateComponent<CustomBeanRegistry.MyBean> {
  private static boolean isDebug() {
    return false;
  }

  private static final int CURRENT_VERSION = 1;
  private static final Logger LOG = Logger.getInstance(CustomBeanRegistry.class);

  private static final String CUSTOM_BEAN_PARSER = "com.intellij.spring.model.xml.custom.CustomBeanParser";
  private Map<String, List<CustomBeanInfo>> myText2Infos = new HashMap<>();
  private Map<MyQName, CustomBeanInfo> myPolicies = new HashMap<>();
  private static final String FAKE_ID = "IntelliJIDEARulezzz";

  public static final String CUSTOM_SPRING_BEANS_PARSING_TIMEOUT = "custom.spring.beans.parsing.timeout";

  private static int getTimeout() {
    try {
      return Integer.parseInt(System.getProperty(CUSTOM_SPRING_BEANS_PARSING_TIMEOUT));
    }
    catch (NumberFormatException e) {
      return isDebug() ? 10000000 : 10000;
    }
  }

  @Override
  public MyBean getState() {
    MyBean bean = new MyBean();
    bean.map = new HashMap<>();
    for (String s : myText2Infos.keySet()) {
      List<CustomBeanInfo> infos = myText2Infos.get(s);
      if (infos != null && !infos.isEmpty()) {
        bean.map.put(s, infos);
      }
    }
    bean.policies = myPolicies;
    return bean;
  }

  @Override
  public void loadState(MyBean state) {
    if (state.version == CURRENT_VERSION) {
      myText2Infos = state.map;
      myPolicies = state.policies;
    }
  }

  @RequiredReadAction
  public ParseResult parseBeans(Collection<XmlTag> tags) {
    ParseResult result = ParseResult.EMPTY_PARSE_RESULT;
    for (XmlTag tag : tags) {
      if (tag.isValid()) {
        result = result.merge(parseBean(tag));
      }
    }
    return result;
  }

  @RequiredReadAction
  public ParseResult parseBean(XmlTag tag) {
    String text = getIdealBeanText(tag);
    try {
      Module module = tag.getModule();
      if (module == null) return ParseResult.EMPTY_PARSE_RESULT;

      ParseResult result = getCustomBeans(createTag(text, tag.getProject()), module);
      myText2Infos.put(text, result.beans == null ? Collections.<CustomBeanInfo>emptyList() : result.beans);
      return result;
    }
    catch (IncorrectOperationException e) {
      return new ParseResult(e);
    }
  }

  public static CustomBeanRegistry getInstance(Project project) {
    return ServiceManager.getService(project, CustomBeanRegistry.class);
  }

  @Nullable
  @RequiredReadAction
  public List<CustomBeanInfo> getParseResult(XmlTag tag) {
    CustomBeanInfo policy = myPolicies.get(new MyQName(tag.getNamespace(), tag.getLocalName()));
    if (policy != null) {
      CustomBeanInfo info = new CustomBeanInfo(policy);
      info.beanName = tag.getAttributeValue(policy.idAttribute);
      return Arrays.asList(info);
    }

    return myText2Infos.get(getIdealBeanText(tag));
  }

  @Nonnull
  @RequiredReadAction
  public static String getIdealBeanText(XmlTag tag) {
    Set<String> usedNamespaces = collectReferencedNamespaces(tag);

    String text = tag.getText();

    try {
      XmlTag copy = createTag(text, tag.getProject());
      XmlTag parent = tag;
      while (parent != null) {
        for (XmlAttribute attribute : parent.getAttributes()) {
          if (attribute.isNamespaceDeclaration()) {
            String prefix = "xmlns".equals(attribute.getName()) ? "" : attribute.getLocalName();
            String ns = copy.getNamespaceByPrefix(prefix);
            if (StringUtil.isEmpty(ns) && usedNamespaces.contains(attribute.getDisplayValue())) {
              copy.add(attribute);
            }
          }
        }
        parent = parent.getParentTag();
      }

      text = copy.getText();
      Document document;
      try {
        document = JDOMUtil.loadDocument(text);
      }
      catch (IOException e) {
        return text;
      }
      catch (JDOMException e) {
        return text;
      }
      return JDOMUtil.writeDocument(document, "\n");
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
      return text;
    }
  }

  private static Set<String> collectReferencedNamespaces(XmlTag tag) {
    final Set<String> usedNamespaces = new HashSet<>();
    tag.accept(new XmlElementVisitor() {
      @Override
      public final void visitXmlTag(XmlTag tag) {
        usedNamespaces.add(tag.getNamespace());
        for (XmlAttribute attribute : tag.getAttributes()) {
          visitXmlAttribute(attribute);
        }
        for (XmlTag xmlTag : tag.getSubTags()) {
          visitXmlTag(xmlTag);
        }
      }

      @Override
      public final void visitXmlAttribute(XmlAttribute attribute) {
        usedNamespaces.add(attribute.getNamespace());
      }
    });
    return usedNamespaces;
  }

  private static XmlTag createTag(String text, Project project) throws IncorrectOperationException {
    return XmlElementFactory.getInstance(project).createTagFromText(text);
  }

  @Nonnull
  @RequiredReadAction
  public static XmlTag getActualSourceTag(CustomBeanInfo info, XmlTag tag) {
    List<Integer> path = info.path;
    for (Integer index : path) {
      XmlTag parent = tag;
      XmlTag[] subTags = parent.getSubTags();
      int i = index;
      tag = subTags[i];
      if (tag == null) {
        LOG.error("parent: " + parent.getText() + "\nindex: " + i + "\nsubTags: " + Arrays.toString(subTags));
      }
    }
    return tag;
  }

  public void addBeanPolicy(@Nonnull String namespace, @Nonnull String localName, CustomBeanInfo info) {
    assert info.beanName == null;
    assert info.idAttribute != null;
    myPolicies.put(new MyQName(namespace, localName), info);
  }

  public static class MyBean {
    public int version = CURRENT_VERSION;

    public Map<String, List<CustomBeanInfo>> map = new HashMap<>();
    public Map<MyQName, CustomBeanInfo> policies = new HashMap<>();
  }

  public static class MyQName {
    public String namespace;
    public String localName;

    public MyQName() {
    }

    public MyQName(String namespace, String localName) {
      this.namespace = namespace;
      this.localName = localName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) return true;
      return o instanceof MyQName that
        && localName.equals(that.localName)
        && namespace.equals(that.namespace);
    }

    @Override
    public int hashCode() {
      int result = namespace.hashCode();
      result = 31 * result + localName.hashCode();
      return result;
    }
  }

  private static void computeUrls(consulo.module.Module module, PathsList list) {
    File springPluginClassesLocation = new File(ClassPathUtil.getJarPathForClass(CustomBeanWrapperImpl.class));
    if (springPluginClassesLocation.isFile()) {//build
      File customNsLocation = new File(springPluginClassesLocation.getParent(), "customNs");
      list.add(new File(customNsLocation, "customNs.jar").getAbsolutePath());
    }
    else {//development mode
      list.add(new File(springPluginClassesLocation.getParent(), "spring-customNs").getAbsolutePath());
    }
    JspSpiUtil.processClassPathItems(null, module, list::add);
    for (VirtualFile file : ModuleRootManager.getInstance(module).getContentFolderFiles(LanguageContentFolderScopes.production())) {
      list.add(file);
    }
  }

  @Nonnull
  @RequiredReadAction
  private static ParseResult getCustomBeans(@Nonnull XmlTag tag, @Nonnull consulo.module.Module module) {
    Map<String, String> handlersToRun = findHandlersToRun(module, tag);
    String namespace = tag.getNamespace();
    if (!handlersToRun.containsKey(namespace)) {
      return new ParseResult(SpringLocalize.parseNoNamespaceHandler(namespace));
    }

    OwnJavaParameters javaParameters = new OwnJavaParameters();
    javaParameters.setJdk(ModuleUtilCore.getSdk(module, JavaModuleExtension.class));
    javaParameters.setMainClass("com.intellij.spring.model.xml.custom.CustomBeanParser");
    if (isDebug()) {
      javaParameters.getVMParametersList().addParametersString("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5239");
    }
    computeUrls(module, javaParameters.getClassPath());
    GeneralCommandLine cmd;
    try {
      cmd = javaParameters.toCommandLine();
    }
    catch (ExecutionException e) {
      return new ParseResult(e);
    }

    ProcessHandler handler = null;
    try {
      handler = ProcessHandlerBuilder.create(cmd).build();

      PrintWriter writer = new PrintWriter(handler.getProcessInput());
      handler.startNotify();

      int timeout = Math.max(getTimeout(), tag.getTextLength() * 150);
      writer.println(timeout);

      ParseResult result = invokeParser(writer, handler, tag, timeout);

      if (result.getStackTrace() != null && tag.getAttributeValue("id") == null) {
        try {
          tag.setAttribute("id", FAKE_ID);
        }
        catch (IncorrectOperationException e) {
          LOG.error(e);
        }
        ParseResult result1 = invokeParser(writer, handler, tag, timeout);
        List<CustomBeanInfo> list = result1.getBeans();
        if (list != null) {
          for (CustomBeanInfo info : list) {
            if (FAKE_ID.equals(info.beanName) && info.path.isEmpty()) {
              info.beanName = null;
              info.idAttribute = "id";
            }
          }
          result = result1;
        }
      }

      List<CustomBeanInfo> infos = result.getBeans();
      if (infos != null) {
        guessIdAttributeNames(writer, handler, tag, infos, timeout);
      }
      return result;
    }
    catch (Throwable e) {
      return new ParseResult(e);
    }
    finally {
      if (handler != null) {
        handler.destroyProcess();
      }
    }
  }

  @Nonnull
  @RequiredReadAction
  private static ParseResult invokeParser(PrintWriter writer, final ProcessHandler handler, XmlTag tag, int timeout) {
    final SimpleReference<ParseResult> result = SimpleReference.create(null);
    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final StringBuilder other = new StringBuilder();

    handler.addProcessListener(new ProcessAdapter() {
      StringBuilder sb = new StringBuilder();

      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        try {
          if (outputType != ProcessOutputTypes.STDOUT) {
            other.append(event.getText());
            return;
          }

          sb.append(event.getText().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
          int i = sb.indexOf("\n\n");
          if (i < 0) return;

          String input = sb.substring(0, i);
          sb.delete(0, i + 2);
          String[] lines = input.split("\n");
          int k = 0;
          while (k < lines.length && !"exception".equals(lines[k]) && !"timeout".equals(lines[k]) && !"result".equals(lines[k])) k++;

          if (k >= lines.length) {
            setResult(new ParseResult(LocalizeValue.localizeTODO("Internal error parsing bean; output:\n" + input)));
            return;
          }

          String first = lines[k];
          if ("exception".equals(first)) {
            assert lines.length == k + 2;
            setResult(new ParseResult(StringEscapeUtil.unescape(lines[k + 1]), true));
            return;
          }
          if ("timeout".equals(first)) {
            assert lines.length == k + 1;
            setResult(new ParseResult(SpringLocalize.timeoutParsingBean()));
            return;
          }
          if ("result".equals(first)) {
            List<CustomBeanInfo> list = new SmartList<>();
            String nextLine = lines[k + 1];
            boolean hasInfras = "has_infrastructures".equals(nextLine);
            if (!hasInfras) {
              assert "no_infrastructures".equals(nextLine);
            }
            int j = k + 2;
            while (j < lines.length) {
              nextLine = lines[j++];
              assert "info".equals(nextLine);
              CustomBeanInfo info = new CustomBeanInfo();
              while (!"info_end".equals(lines[j])) {
                String prop = lines[j++];
                String propValue = StringEscapeUtil.unescape(lines[j++]);
                if ("beanName".equals(prop)) {
                  info.beanName = propValue;
                }
                else if ("beanClassName".equals(prop)) {
                  info.beanClassName = propValue;
                }
                else if ("constructorArgumentCount".equals(prop)) {
                  info.constructorArgumentCount = Integer.parseInt(propValue);
                }
                else if ("factoryMethodName".equals(prop)) {
                  info.factoryMethodName = propValue;
                }
                else if ("factoryBeanName".equals(prop)) {
                  info.factoryBeanName = propValue;
                }
                else {
                  assert "path".equals(prop) : prop;
                  assert propValue.startsWith("x"); //otherwise string may be empty
                  String separated = propValue.substring(1);
                  info.path = StringUtil.isEmpty(separated)
                    ? Collections.<Integer>emptyList()
                    : ContainerUtil.map(separated.split(";"), Integer::parseInt);
                }
              }
              list.add(info);
              j++;
            }
            setResult(new ParseResult(list, hasInfras));
          }
        }
        catch (Throwable e) {
          setResult(new ParseResult(e));
        }
      }

      private void setResult(ParseResult value) {
        result.set(value);
        handler.removeProcessListener(this);
        semaphore.up();
      }

      @Override
      public void processTerminated(ProcessEvent event) {
        if (other.length() == 0 || sb.length() == 0) {
          setResult(new ParseResult(SpringLocalize.processUnexpectedlyTerminated("")));
          return;
        }
        String output = ":\n\nSTDOUT:\n" + sb + "\n\nOTHER:\n" + other;
        setResult(new ParseResult(SpringLocalize.processUnexpectedlyTerminated(output)));
      }
    });

    writer.println("input");
    writer.println(StringEscapeUtil.escape(tag.getText(), '"'));
    writer.flush();

    boolean inTime = semaphore.waitFor(timeout);

    ParseResult parseResult = result.get();
    if (parseResult == null) {
      if (inTime) {
        return new ParseResult(other.toString(), true);
      }

      return new ParseResult(SpringLocalize.timeoutParsingBean());
    }
    return parseResult;
  }

  @RequiredReadAction
  private static void guessIdAttributeNames(PrintWriter writer,
                                            ProcessHandler reader,
                                            XmlTag tag,
                                            List<CustomBeanInfo> list,
                                            int timeout)
    throws IncorrectOperationException {
    String[] fakeNames = new String[list.size()];
    String[] idAttrs = new String[list.size()];
    boolean hasFakeIds = false;
    for (int i = 0; i < list.size(); i++) {
      CustomBeanInfo info = list.get(i);
      if (info.idAttribute != null) continue;

      XmlTag sourceTag = getActualSourceTag(info, tag);
      String id = info.beanName;
      XmlAttribute idAttr = id == null ? null : ContainerUtil.find(
        sourceTag.getAttributes(),
        xmlAttribute -> !xmlAttribute.isNamespaceDeclaration() && id.equals(xmlAttribute.getDisplayValue())
      );
      if (idAttr != null) {
        String fakeName = FAKE_ID + i;
        fakeNames[i] = fakeName;
        idAttr.setValue(fakeName);
        idAttrs[i] = idAttr.getLocalName();
        hasFakeIds = true;
      }
    }

    if (hasFakeIds) {
      List<CustomBeanInfo> withFakes = invokeParser(writer, reader, tag, timeout).getBeans();
      if (withFakes != null && withFakes.size() == list.size()) {
        for (int i = 0; i < fakeNames.length; i++) {
          String name = fakeNames[i];
          if (name != null && name.equals(withFakes.get(i).beanName)) {
            list.get(i).idAttribute = idAttrs[i];
          }
        }
      }
    }
  }

  private static Map<String, String> findHandlersToRun(@Nonnull Module module, @Nonnull XmlTag tag) {
    if (SpringConstants.INSIDER_NAMESPACES.contains(tag.getNamespace())) return Collections.emptyMap();

    Map<String, String> handlers = SpringSchemaProvider.getHandlers(module);
    if (handlers.isEmpty()) return Collections.emptyMap();

    Set<String> referencedNamespaces = collectReferencedNamespaces(tag);

    Map<String, String> handlersToRun = new HashMap<>(referencedNamespaces.size());
    for (String namespace : handlers.keySet()) {
      if (referencedNamespaces.contains(namespace)) {
        handlersToRun.put(namespace, handlers.get(namespace));
      }
    }

    return handlersToRun;
  }

  public static class ParseResult {
    static final ParseResult EMPTY_PARSE_RESULT = new ParseResult(Collections.<CustomBeanInfo>emptyList(), false);

    @Nullable
    List<CustomBeanInfo> beans;
    boolean hasInfrastructures;
    @Nullable
    String errorMessage;
    @Nullable
    String stackTrace;

    private ParseResult(List<CustomBeanInfo> beans, boolean hasInfrastructures) {
      this.beans = beans;
      this.hasInfrastructures = hasInfrastructures;
    }

    private static String getStackTrace(Throwable e) {
      return ExceptionUtil.getThrowableText(e);
    }

    private ParseResult(Throwable t) {
      this(getStackTrace(t), true);
    }

    private ParseResult(LocalizeValue errorMessage) {
      this(errorMessage.get(), false);
    }

    private ParseResult(@Nullable String errorMessage, boolean isStackTrace) {
      if (!isStackTrace) {
        this.errorMessage = errorMessage;
      }
      else {
        stackTrace = StringUtil.convertLineSeparators(errorMessage);
        int i = stackTrace.indexOf(CUSTOM_BEAN_PARSER);
        if (i >= 0) {
          i = stackTrace.lastIndexOf('\n', i);
          if (i >= 0) {
            stackTrace = stackTrace.substring(0, i);
          }
        }
      }
    }

    @Nullable
    public String getErrorMessage() {
      return errorMessage;
    }

    @Nullable
    public String getStackTrace() {
      return stackTrace;
    }

    @Nullable
    public List<CustomBeanInfo> getBeans() {
      return beans;
    }

    public boolean hasInfrastructureBeans() {
      return hasInfrastructures;
    }

    public boolean hasErrors() {
      return stackTrace != null || errorMessage != null;
    }

    public ParseResult merge(ParseResult with) {
      ParseResult result = new ParseResult((List<CustomBeanInfo>)null, hasInfrastructures || with.hasInfrastructures);
      result.stackTrace = stackTrace == null ? with.stackTrace : stackTrace;
      result.errorMessage = errorMessage == null ? with.errorMessage : errorMessage;
      result.beans = beans == null ? with.beans : with.beans == null ? beans : ContainerUtil.concat(beans, with.beans);
      return result;
    }
  }
}


