/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.perspectives.graph.SpringBeanDependenciesDataModel;
import com.intellij.spring.perspectives.graph.SpringBeanDependencyInfo;
import com.intellij.spring.perspectives.graph.SpringBeanDependenciesDiagramContext;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class SpringModelTest extends LightSpringTestCase {
  private Beans myBeans;

  protected void setUp() throws Exception {
    super.setUp();                             
    myBeans = getFileElement("spring-beans-names-and-aliases.xml", Beans.class, myProject).getRootElement();
  }

  protected void tearDown() throws Exception {
    myBeans = null;
    super.tearDown();
  }

  public void testSimpleId() {
    SpringBean springBean = findBeanById("simpleId");
    assertNotNull(springBean);

    Set<String> list = SpringUtils.getAllBeanNames(springBean);
    assertEquals(list.size(), 1);
    assertEquals(list.iterator().next(), "simpleId");
  }

  public void testSimpleName() {
    SpringBean springBean = findBeanByName("simpleName");

    Set<String> list = SpringUtils.getAllBeanNames(springBean);
    assertEquals(list.size(), 1);
    assertEquals(list.iterator().next(), "simpleName");
  }

  public void testMultipleNames() {
    assertNames(SpringUtils.getAllBeanNames(findBeanById("test_with_names")), Arrays.asList("test_with_names", "test1_1", "test1_2", "test1_3"));
    assertNames(SpringUtils.getAllBeanNames(findBeanByName("test2_1")), Arrays.asList("test2_1", "test2_2", "test2_3"));
    assertNames(SpringUtils.getAllBeanNames(findBeanByName("test3_1")), Arrays.asList("test3_1", "test3_2", "test3_3"));

  }

  public void testAliasesNames() {
    assertNames(SpringUtils.getAllBeanNames(findBeanByName("test_alias")),
                Arrays.asList("test_alias", "aliasName_1", "aliasName_2", "aliasName_3"));
  }

  private void assertNames(Collection<String> facts, Collection<String> required) {
    assertEquals(facts.toString(), required.size(), facts.size());

    for (String s : facts) {
        assertTrue("Not equals: " + facts +" and " + required, required.contains(s.trim()));
    }
  }

  private SpringBean findBeanByName(String beanName) {
    SpringBean beanByName = null;

    for (SpringBean bean : myBeans.getBeans()) {
      String currentBeanName = bean.getName().getStringValue();

      if (currentBeanName != null && currentBeanName.contains(beanName)) {
        beanByName = bean;
        break;
      }
    }
    assertNotNull(beanByName);

    return beanByName;
  }

  private SpringBean findBeanById(@NotNull String beanId) {
    return findBean(myBeans, beanId);
  }

  public void testBeans() {
    getFileElement("spring-beans-1.0.xml", Beans.class, myProject);
  }

  public void testGraphModel() throws Exception {
    DomFileElement<Beans> fileElement = getFileElement("generated_spring_bean.xml", Beans.class, myProject);
    SpringBeanDependenciesDataModel model = new SpringBeanDependenciesDataModel(fileElement.getFile(),
                                                                                      SpringBeanDependenciesDiagramContext.DEFAULT);
//    ProfilingUtil.forceCPUTracing();

    long start = System.currentTimeMillis();
    model.updateDataModel();
    long doneFor = System.currentTimeMillis() - start;
    System.out.println("Model built in " + doneFor + "ms");

    Collection<SpringBaseBeanPointer> nodes = model.getNodes();
    Collection<SpringBeanDependencyInfo> edges = model.getEdges();
    assertEquals(99, nodes.size());
    assertEquals(243, edges.size());
//    ProfilingUtil.forceStopCPUTracing(name);
  }


  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/modelTest/";
  }
}