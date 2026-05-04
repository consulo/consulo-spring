package com.intellij.spring.model.xml.generate;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.actions.GenerateSpringBeanBodyAction;
import com.intellij.spring.model.actions.generate.GenerateSpringBeanDependenciesUtil;
import com.intellij.spring.model.actions.generate.SpringBeanClassMember;
import com.intellij.spring.model.actions.generate.SpringGenerateTemplatesHolder;
import com.intellij.spring.model.actions.generate.SpringPropertiesGenerateProvider;
import com.intellij.spring.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import java.util.function.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomManager;

import java.io.IOException;
import java.util.*;

public class GenerateSpringModelElementsTest extends SpringHighlightingTestCase {
  private static final String[] springJarTestNames = new String[]{"testFactoryBeanProperties", "testCompiledClassesDependencies",
    "testFactoryBeanDependencies", "testFactoryBeanConstructorDependencies", "testFactoryBeanConstructorDependenciesForPsiClass", "testFactoryBeanSetterDependencies"};
  private static final String[] hibernateJarTestNames = new String[]{"testFactoryBeanProperties", "testCompiledClassesDependencies",
    "testFactoryBeanDependencies", "testFactoryBeanConstructorDependencies", "testFactoryBeanConstructorDependenciesForPsiClass", "testFactoryBeanSetterDependencies"};

  protected boolean isWithTestSources() {
    return !Arrays.asList("testSetterDependencies",
                          "testConstructorDependencies",
                          "testConstructorDependenciesSimple",
                          "testConstructorDependenciesSimple2",
                          "testConstructorDependenciesAutowire").contains(getName());
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    for (String springJarTestName : springJarTestNames) {
      if (springJarTestName.equals(getName())) {
        addSpringJar(moduleBuilder);
        break;
      }
    }
    for (String hibernateJarTestName : hibernateJarTestNames) {
      if (hibernateJarTestName.equals(getName())) {
        moduleBuilder.addLibraryJars("hibernate3", PathManager.getHomePath() + "/lib/", "hibernate3.jar");
        break;
      }
    }
  }

  protected void doGenerationTest(String file, String expected) throws Throwable {
    myFixture.configureByFile(file);
    final GenerateSpringBeanBodyAction action = new GenerateSpringBeanBodyAction(new SpringPropertiesGenerateProvider());
    assertTrue(action.isValidForFile(myProject, myFixture.getEditor(), myFixture.getFile()));

    final int offset = myFixture.getEditor().getCaretModel().getOffset();
    final PsiElement element = myFixture.getFile().findElementAt(offset);
    final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
    final SpringBean springBean = (SpringBean)DomManager.getDomManager(myProject).getDomElement(tag);

    final Collection<PsiMethod> psiMethods = SpringPropertiesGenerateProvider.getNonInjectedPropertySetters(springBean);

    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        SpringPropertiesGenerateProvider.doGenerate(myFixture.getEditor(), springBean, myProject, psiMethods.toArray(PsiMethod.EMPTY_ARRAY));
      }
    }.execute();
    myFixture.checkResultByFile(expected);
    assertFalse(action.isValidForFile(myProject, myFixture.getEditor(), myFixture.getFile()));
  }

  public void testPrimitiveProperties() throws Throwable {
    doGenerationTest("boolean-property.xml", "boolean-property_after.xml");
  }

  public void testCollectionsProperties() throws Throwable {
    doGenerationTest("collections-properties.xml", "collections-properties_after.xml");
  }

  public void testReferencedProperties() throws Throwable {
    doGenerationTest("referenced-properties.xml", "referenced-properties_after.xml");
  }

  public void testFactoryBeanProperties() throws Throwable {
    doGenerationTest("factory-properties.xml", "factory-properties_after.xml");
  }

  public void testFactoryBeanSetterDependencies() throws IOException {
    final Beans beans = getBeans("primitive-properties.xml");

    final SpringBean transactionManager = findBean(beans, "transactionManager");
    final CommonSpringBean sessionFactory = findBean(beans, "sessionFactory");

    assertNull(SpringUtils.findPropertyByName(transactionManager, "sessionFactory", true));
    final List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = generateDependencies(transactionManager, Collections.singletonList(sessionFactory), true);
    SpringProperty springProperty = (SpringProperty)list.get(0).getFirst();

    assertEquals(springProperty.getName().getStringValue(), "sessionFactory");
    assertEquals(springProperty.getRefAttr().getStringValue(), "sessionFactory");
  }

  public void testFactoryBeanConstructorDependencies() throws IOException {
    final Beans beans = getBeans("primitive-properties.xml");

    final SpringBean transactionManager = findBean(beans, "transactionManager");
    final CommonSpringBean sessionFactory = findBean(beans, "sessionFactory");
    List<SpringBeanClassMember> candidates = GenerateSpringBeanDependenciesUtil.getCandidates(transactionManager, false);
    assertEquals(candidates.size(), 1);
    assertEquals(candidates.get(0).getSpringBean().getSpringBean(), sessionFactory);

    assertEquals(SpringUtils.getConstructorArgs(transactionManager).size(), 0);
    final List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = generateDependencies(transactionManager, Collections.singletonList(sessionFactory), false);
    assertEquals(SpringUtils.getConstructorArgs(transactionManager).size(), 1);
    ConstructorArg arg = (ConstructorArg)list.get(0).getFirst();

    assertEquals(arg.getRefAttr().getStringValue(), "sessionFactory");
  }

  public void testFactoryBeanConstructorDependenciesForPsiClass() throws IOException {
    final String tmClassName = "org.springframework.orm.hibernate3.HibernateTransactionManager";
    SpringModel springModel = SpringManager.getInstance(myProject).getLocalSpringModel(getXmlFile("session-factory-spring-config.xml"));

    assertNotNull(springModel);
    List<SpringBeanPointer> transactionManagerList = SpringUtils.findBeansByClassName(springModel.getAllCommonBeans(true), tmClassName);
    assertEquals(transactionManagerList.size(), 0);

    PsiClass transactionManagerClass = JavaPsiFacade.getInstance(myProject).findClass(tmClassName, GlobalSearchScope.allScope(myProject));
    assertNotNull(transactionManagerClass);

    List<SpringBeanClassMember> candidates = GenerateSpringBeanDependenciesUtil.getCandidates(springModel, transactionManagerClass, false);
    assertEquals(candidates.size(), 1);
    final CommonSpringBean springBean = springModel.getMergedModel().getBeans().get(0);
    createBeanAndGenerateDependencies(transactionManagerClass, Collections.singletonList(springBean), false);

    springModel = SpringManager.getInstance(myProject).getLocalSpringModel(getXmlFile("session-factory-spring-config.xml"));
    transactionManagerList = SpringUtils.findBeansByClassName(springModel.getAllCommonBeans(true), tmClassName);
    assertEquals(transactionManagerList.size(), 1);

    SpringBean transactionManager = (SpringBean)transactionManagerList.get(0).getSpringBean();
    assertEquals(SpringUtils.getConstructorArgs(transactionManager).size(), 1);
  }

  public void testSetterDependencies() throws Throwable {
    myFixture.configureByFiles("setter-dependencies.xml", "Foo.java", "Foo2.java", "Foo3.java", "Foo4.java");
    final Beans beans = getBeans("setter-dependencies.xml");
    final CommonSpringBean foo = findBean(beans, "foo");
    final SpringBean foo2_has_foo_setter = findBean(beans, "foo2");
    assertNull(SpringUtils.findPropertyByName(foo2_has_foo_setter, "foo", true));

    assertNotNull(findPropertySetter(foo2_has_foo_setter, "foo"));

    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = generateDependencies(foo2_has_foo_setter, Collections.singletonList(foo), true);

    SpringProperty springProperty = (SpringProperty)list.get(0).getFirst();

    assertEquals(springProperty.getName().getStringValue(), "foo");
    assertEquals(springProperty.getRefAttr().getStringValue(), "foo");

    final SpringBean foo3_has_no_foo_setter = findBean(beans, "foo3");
    assertNull(SpringUtils.findPropertyByName(foo3_has_no_foo_setter, "foo", true));

    assertNull(findPropertySetter(foo3_has_no_foo_setter, "foo"));

    list = generateDependencies(foo3_has_no_foo_setter, Collections.singletonList(foo), true);
    springProperty = (SpringProperty)list.get(0).getFirst();

    assertEquals(springProperty.getName().getStringValue(), "foo");
    assertEquals(springProperty.getRefAttr().getStringValue(), "foo");
    PsiMethod propertySetter = findPropertySetter(foo3_has_no_foo_setter, "foo");

    assertNotNull(propertySetter);
    assertTrue(PropertyUtil.isSimplePropertySetter(propertySetter));
    assertTrue(propertySetter.getParameterList().getParameters()[0].getType().getCanonicalText().equals("Foo"));

    final SpringBean foo4_has_foo_setter_with_wrong_type = findBean(beans, "foo4");
    propertySetter = findPropertySetter(foo4_has_foo_setter_with_wrong_type, "foo");
    assertNotNull(propertySetter);
    assertTrue(propertySetter.getParameterList().getParameters()[0].getType().getCanonicalText().equals("java.lang.String"));

    list = generateDependencies(foo4_has_foo_setter_with_wrong_type, Collections.singletonList(foo), true);
    springProperty = (SpringProperty)list.get(0).getFirst();

    assertEquals(springProperty.getName().getStringValue(), "foo");
    assertEquals(springProperty.getRefAttr().getStringValue(), "foo");
    propertySetter = findPropertySetter(foo4_has_foo_setter_with_wrong_type, "foo", "Foo");

    assertNotNull(propertySetter);
    assertTrue(PropertyUtil.isSimplePropertySetter(propertySetter));
    assertTrue(propertySetter.getParameterList().getParameters()[0].getType().getCanonicalText().equals("Foo"));
  }

  public void testCandidatesWithNoId() throws IOException {
    Beans beans = getBeans("beans-with-no-id.xml");

    assertEquals(3, GenerateSpringBeanDependenciesUtil.getCandidates(findBean(beans, "foo"), true).size());

    beans = getBeans("beans-with-no-id-2.xml");
    assertEquals(4, GenerateSpringBeanDependenciesUtil.getCandidates(findBean(beans, "foo"), true).size());
  }

  public void testConstructorDependencies() throws Throwable {
    myFixture.configureByFiles("constructor-dependencies.xml", "Foo.java", "Foo2.java", "Foo3.java",
                               "Foo4.java", "Foo5.java", "FooEmptyConstructor.java",
                               "FooSimpleConstructor.java");
    final Beans beans = getBeans("constructor-dependencies.xml");
    final CommonSpringBean foo = findBean(beans, "foo");

    final SpringBean foo_empty_constructor = findBean(beans, "fooEmptyConstructor");
    assertEquals(0, foo_empty_constructor.getBeanClass().getConstructors().length);

    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = generateDependencies(foo_empty_constructor, Collections.singletonList(foo), false);
    assertEquals(list.size(), 1);
    ConstructorArg constructorArg = (ConstructorArg)list.get(0).getFirst();

    assertEquals(constructorArg.getRefAttr().getStringValue(), "foo");

  }

  public void testConstructorDependenciesSimple() throws Throwable {
    myFixture.configureByFiles("Foo.java", "Foo2.java", "Foo3.java", "Foo4.java", "Foo5.java",
                               "FooEmptyConstructor.java", "FooSimpleConstructor.java");
    final Beans beans = getBeans("constructor-dependencies.xml");
    final CommonSpringBean foo = findBean(beans, "foo");

    final SpringBean foo_simple_constructor = findBean(beans, "fooSimpleConstructor");
    List<ConstructorArg> constructorArgs = foo_simple_constructor.getConstructorArgs();
    assertEquals(constructorArgs.size(), 1);
    assertEquals(constructorArgs.get(0).getRefAttr().getStringValue(), "foo2");

    PsiMethod[] psiMethods = foo_simple_constructor.getBeanClass().getConstructors();
    assertEquals(psiMethods.length, 1);
    PsiParameter[] parameters = psiMethods[0].getParameterList().getParameters();
    assertEquals(parameters.length, 1);

    assertEquals(parameters[0].getType().getCanonicalText(), "Foo2");
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = generateDependencies(foo_simple_constructor, Collections.singletonList(foo), false);
    assertEquals(list.size(), 1);

    constructorArgs = foo_simple_constructor.getConstructorArgs();
    ConstructorArg arg = constructorArgs.get(0);
    ConstructorArg arg2 = constructorArgs.get(1);

    assertEquals(arg.getRefAttr().getStringValue(), "foo2");
    assertEquals(arg2.getRefAttr().getStringValue(), "foo");

    psiMethods = foo_simple_constructor.getBeanClass().getConstructors();
    assertEquals(psiMethods.length, 1);
    parameters = psiMethods[0].getParameterList().getParameters();
    assertEquals(parameters.length, 2);

    assertEquals(parameters[0].getType().getCanonicalText(), "Foo2");
    assertEquals(parameters[1].getType().getCanonicalText(), "Foo");
  }

  public void testConstructorDependenciesSimple2() throws Throwable {
    myFixture.configureByFiles("Foo.java", "Foo2.java", "Foo3.java", "Foo4.java", "Foo5.java",
                               "FooEmptyConstructor.java", "FooSimpleConstructor.java");
    final Beans beans = getBeans("constructor-dependencies.xml");
    final CommonSpringBean foo = findBean(beans, "foo");

    final SpringBean foo_simple_constructor = findBean(beans, "fooSimpleConstructor2");
    List<ConstructorArg> args = foo_simple_constructor.getConstructorArgs();
    assertEquals(args.size(), 1);
    assertEquals(args.get(0).getRefAttr().getStringValue(), "foo3");

    PsiMethod[] psiMethods = foo_simple_constructor.getBeanClass().getConstructors();
    assertEquals(psiMethods.length, 1);
    PsiParameter[] parameters = psiMethods[0].getParameterList().getParameters();
    assertEquals(parameters.length, 1);

    assertEquals(parameters[0].getType().getCanonicalText(), "Foo2");

    generateDependencies(foo_simple_constructor, Collections.singletonList(foo), false);

    List<ConstructorArg> constructorArgs = foo_simple_constructor.getConstructorArgs();
    assertEquals(constructorArgs.size(), 2);

    ConstructorArg arg = constructorArgs.get(0);
    ConstructorArg arg2 = constructorArgs.get(1);

    assertEquals(arg.getRefAttr().getStringValue(), "foo3");
    assertEquals(arg2.getRefAttr().getStringValue(), "foo");

    psiMethods = foo_simple_constructor.getBeanClass().getConstructors();
    assertEquals(psiMethods.length, 2);
    parameters = psiMethods[1].getParameterList().getParameters();
    assertEquals(parameters.length, 2);

    assertEquals(parameters[0].getType().getCanonicalText(), "Foo3");
    assertEquals(parameters[1].getType().getCanonicalText(), "Foo");
  }

  public void testConstructorDependenciesAutowire() throws Throwable {
    myFixture.configureByFiles("constructor-dependencies.xml", "Foo.java", "Foo2.java", "Foo3.java", "Foo4.java",
                               "Foo5.java", "FooEmptyConstructor.java", "FooAutowiredConstructor.java");
    final Beans beans = getBeans("constructor-dependencies.xml");
    final CommonSpringBean foo5 = findBean(beans, "foo5");

    // 2 args are definde (1,3) and 2 are autowired (2,4)
    final SpringBean foo_autowired_constructor = findBean(beans, "fooAutowiredConstructor");
    assertEquals(foo_autowired_constructor.getConstructorArgs().size(), 2);

    PsiMethod[] psiMethods = foo_autowired_constructor.getBeanClass().getConstructors();
    assertEquals(1, psiMethods.length);
    PsiParameter[] parameters = psiMethods[0].getParameterList().getParameters();
    assertEquals(4, parameters.length);

    // foo_autowired_constructor is resoved to this constuctor
    assertEquals(SpringConstructorArgResolveUtil.findMatchingMethods(foo_autowired_constructor).size(), 1);

    generateDependencies(foo_autowired_constructor, Collections.singletonList(foo5), false);
    assertEquals(foo_autowired_constructor.getConstructorArgs().size(), 3);

    psiMethods = foo_autowired_constructor.getBeanClass().getConstructors();
    assertEquals(psiMethods.length, 1);

    parameters = psiMethods[0].getParameterList().getParameters();
    assertEquals(parameters.length, 5);

    assertEquals(parameters[4].getType().getCanonicalText(), "Foo5");
  }

   private List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependencies(final SpringBean source,
                                                     final List<CommonSpringBean> targets,
                                                     final boolean isSetterDependencies) {
    final List<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>> list = new ArrayList<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>>();
    new WriteCommandAction(source.getManager().getProject()) {
      protected void run(final Result result) throws Throwable {
        list.add(GenerateSpringBeanDependenciesUtil.generateDependencies(source, ContainerUtil.map(targets, new Function<CommonSpringBean, SpringBeanPointer>() {
          public SpringBeanPointer fun(final CommonSpringBean bean) {
            return SpringBeanPointer.createSpringBeanPointer(bean);
          }
        }), isSetterDependencies));
      }
    }.execute();

    return list.get(0);
  }

  private List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> createBeanAndGenerateDependencies(final PsiClass source,
                                                                  final List<CommonSpringBean> targets,
                                                                  final boolean isSetterDependencies) {
    final List<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>> list = new ArrayList<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>>();
    new WriteCommandAction(source.getProject()) {
      protected void run(final Result result) throws Throwable {
        list.add(GenerateSpringBeanDependenciesUtil.createBeanAndGenerateDependencies(source, isSetterDependencies, ContainerUtil.map(targets, new Function<CommonSpringBean, SpringBeanPointer>() {
          public SpringBeanPointer fun(final CommonSpringBean bean) {
            return SpringBeanPointer.createSpringBeanPointer(bean);
          }
        })));
      }
    }.execute();

    return list.get(0);
  }


  public void testCompiledClassesDependencies() throws IOException {
    final Beans beans = getBeans("compiled-classes-dependencies.xml");

    assertFalse(GenerateSpringBeanDependenciesUtil.acceptBean(findBean(beans, "sessionFactory"), true));
    assertEquals(GenerateSpringBeanDependenciesUtil.getCandidates(findBean(beans, "transactionManager"), true).size(), 1);
    assertEquals(GenerateSpringBeanDependenciesUtil.getCandidates(findBean(beans, "foo"), true).size(), 2);
  }

  private PsiMethod findPropertySetter(final SpringBean bean, final String propertyName) {
    final PsiClass beanClass = bean.getBeanClass();
    assertNotNull(beanClass);
    for (PsiMethod psiMethod : beanClass.getAllMethods()) {
      if (PropertyUtil.isSimplePropertySetter(psiMethod) && propertyName.equals(PropertyUtil.getPropertyNameBySetter(psiMethod))) {
        return psiMethod;
      }
    }
    return null;
  }

  private PsiMethod findPropertySetter(final SpringBean bean, final String propertyName, final String className) {
    final PsiClass beanClass = bean.getBeanClass();
    assertNotNull(beanClass);
    for (PsiMethod psiMethod : beanClass.getAllMethods()) {
      if (PropertyUtil.isSimplePropertySetter(psiMethod) && propertyName.equals(PropertyUtil.getPropertyNameBySetter(psiMethod)) &&
          psiMethod.getParameterList().getParameters()[0].getType().getCanonicalText().equals(className)) {
        return psiMethod;
      }
    }
    return null;
  }

  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/generate/";
  }
}
