/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.factories;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.factories.resolvers.*;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.ide.ServiceManager;
import consulo.internal.org.objectweb.asm.*;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.XmlSerializer;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * @author Serega Vasiliev, Taras Tielkes
 */
@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class SpringFactoryBeansManager {
  private static final Key<CachedValue<Set<String>>> CACHED_OBJECT_TYPE = Key.create("CACHED_OBJECT_TYPE");

  @NonNls
  private static final String BEAN_FACTORY_CLASSNAME = "org.springframework.beans.factory.FactoryBean";

  private final Map<String, ObjectTypeResolver> mySpringFactories = new HashMap<String, ObjectTypeResolver>();
  private final ObjectTypeResolver[] myCustomResolvers = new ObjectTypeResolver[]{new TransactionProxyFactoryBeanTypeResolver(),
    new JndiObjectFactoryBeanTypeResolver(), new SpringEjbTypeResolver(), new ProxyFactoryBeanTypeResolver(),
    new ScopedProxyFactoryBeanTypeResolver(), new BeanReferenceFactoryBeanTypeResolver(), new UtilConstantTypeResolver()};

  @NonNls
  private static final String FACTORIES_RESOURCE_XML = "/resources/factories/factories.xml";
  @NonNls
  private static final String PROPERTY_NAME_DELIMITER = ",";

  @Inject
  public SpringFactoryBeansManager() {
    FactoriesBean factoriesBean =
      XmlSerializer.deserialize(SpringFactoryBeansManager.class.getResource(FACTORIES_RESOURCE_XML), FactoriesBean.class);

    assert factoriesBean != null;
    assert factoriesBean.getFactories() != null;

    for (FactoryBeanInfo factoryBeanInfo : factoriesBean.getFactories()) {
      String factory = factoryBeanInfo.getFactory();
      if (factory != null && factory.trim().length() > 0) {
        mySpringFactories.put(factory, getObjectTypeResolver(factoryBeanInfo));
      }
    }
  }

  @Nullable
  private ObjectTypeResolver getObjectTypeResolver(FactoryBeanInfo factoryBeanInfo) {
    String type = factoryBeanInfo.getObjectType();
    if (!StringUtil.isEmptyOrSpaces(type)) {
      return new SingleObjectTypeResolver(type);
    }

    String delimitedNames = factoryBeanInfo.getPropertyNames();
    if (!StringUtil.isEmptyOrSpaces(delimitedNames)) {
      return new FactoryPropertiesDependentTypeResolver(StringUtil.split(delimitedNames, PROPERTY_NAME_DELIMITER));
    }

    String factoryClass = factoryBeanInfo.getFactory();
    for (ObjectTypeResolver customResolver : myCustomResolvers) {
      if (customResolver.accept(factoryClass)) return customResolver;
    }

    return null;
  }

  public static boolean isBeanFactory(@Nonnull PsiClass psiClass) {
    Project project = psiClass.getProject();
    PsiClass beanFactoryClass =
      JavaPsiFacade.getInstance(project).findClass(BEAN_FACTORY_CLASSNAME, GlobalSearchScope.allScope(project));

    return beanFactoryClass != null && psiClass.isInheritor(beanFactoryClass, true);
  }

  public boolean isProductKnown(@Nonnull PsiClass factoryClass, @Nonnull CommonSpringBean context) {
    return !getProductTypeClassNames(factoryClass, context).isEmpty();
  }

  @Nonnull
  public PsiClass[] getProductTypes(PsiClass factoryClass, @Nonnull CommonSpringBean context) {
    Set<String> typeClassNames = getProductTypeClassNames(factoryClass, context);

    if (typeClassNames.isEmpty()) {
      return PsiClass.EMPTY_ARRAY;
    }
    else {
      List<PsiClass> psiClasses = new ArrayList<PsiClass>(typeClassNames.size());
      Project project = factoryClass.getProject();
      PsiManager psiManager = PsiManager.getInstance(project);
      GlobalSearchScope scope = GlobalSearchScope.allScope(project);
      for (String typeClassName : typeClassNames) {
        PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(typeClassName, scope);
        if (psiClass != null) {
          psiClasses.add(psiClass);
        }
      }
      return psiClasses.toArray(PsiClass.EMPTY_ARRAY);
    }
  }

  public boolean canProduce(@Nonnull PsiClass factory, @Nonnull PsiClass requiredClass, @Nonnull CommonSpringBean context) {
    Set<String> typeNames = getProductTypeClassNames(factory, context);
    if (!typeNames.isEmpty()) {
      if (typeNames.contains(requiredClass.getQualifiedName())) return true;

      Project project = factory.getProject();
      for (String typeName : typeNames) {
        PsiClass productClass = JavaPsiFacade.getInstance(project).findClass(typeName, GlobalSearchScope.allScope(project));
        if (productClass != null && productClass.isInheritor(requiredClass, true)) return true;
      }
    }
    return false;
  }

  public boolean canProduceAny(@Nonnull PsiClass factory, @Nonnull List<PsiClass> requiredClasses, @Nonnull CommonSpringBean context) {
    for (PsiClass requiredClass : requiredClasses) {
      if (requiredClass != null && canProduce(factory, requiredClass, context)) return true;
    }
    return false;
  }

  public boolean isFactoryRegistered(@Nonnull PsiClass factoryClass) {
    return mySpringFactories.containsKey(factoryClass.getQualifiedName());
  }

  @Nonnull
  public Set<String> getProductTypeClassNames(@Nonnull PsiClass factoryClass, @Nonnull CommonSpringBean context) {
    String qualifiedName = factoryClass.getQualifiedName();
    ObjectTypeResolver typeResolver = mySpringFactories.get(qualifiedName);
    if (typeResolver != null) {
      return typeResolver.getObjectType(context);
    }

    PsiManager psiManager = PsiManager.getInstance(factoryClass.getProject());
    for (String factoryClassName : mySpringFactories.keySet()) {
      PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject())
                                             .findClass(factoryClassName, GlobalSearchScope.allScope(factoryClass.getProject()));
      if (psiClass != null && factoryClass.isInheritor(psiClass, false)) {
        ObjectTypeResolver resolver = mySpringFactories.get(factoryClassName);
        if (resolver != null) {
          return resolver.getObjectType(context);
        }
      }
    }

    return guessObjectType(factoryClass);
  }

  private static Set<String> guessObjectType(final PsiClass factoryClass) {
    CachedValue<Set<String>> cachedValue = factoryClass.getUserData(CACHED_OBJECT_TYPE);
    if (cachedValue == null) {
      factoryClass.putUserData(CACHED_OBJECT_TYPE,
                               cachedValue = CachedValuesManager.getManager(factoryClass.getProject())
                                                                .createCachedValue(new CachedValueProvider<Set<String>>() {
                                                                  public Result<Set<String>> compute() {
                                                                    return new Result<Set<String>>(doGuessObjectType(factoryClass),
                                                                                                   factoryClass);
                                                                  }
                                                                }, false));
    }

    return cachedValue.getValue();
  }

  @Nullable
  private static PsiMethod getProductTypeMethod(PsiClass factoryClass) {
    for (PsiMethod psiMethod : factoryClass.findMethodsByName("getObjectType", true)) {
      if (psiMethod.getParameterList().getParameters().length == 0) {
        return psiMethod;
      }
    }
    return null;
  }

  private static Set<String> doGuessObjectType(PsiClass factoryClass) {
    PsiMethod method = getProductTypeMethod(factoryClass);
    if (method == null) return Collections.emptySet();

    if (method instanceof PsiCompiledElement) {
      VirtualFile file = method.getContainingFile().getVirtualFile();
      if (file != null) {
        FactoryBeanObjectTypeReader reader = new FactoryBeanObjectTypeReader();
        try {
          new ClassReader(file.contentsToByteArray()).accept(reader, ClassReader.SKIP_DEBUG);
        }
        catch (IOException e) {
        }
        String qName = reader.getResultQName();
        if (qName != null) return Collections.singleton(qName);
      }
    }

    PsiCodeBlock body = method.getBody();
    if (body != null) {
      PsiStatement[] statements = body.getStatements();
      if (statements.length == 1 && statements[0] instanceof PsiReturnStatement) {
        PsiExpression value = ((PsiReturnStatement)statements[0]).getReturnValue();
        if (value instanceof PsiClassObjectAccessExpression) {
          String s = ((PsiClassObjectAccessExpression)value).getOperand().getType().getCanonicalText();
          if (s != null) {
            return Collections.singleton(s);
          }
        }
      }
    }
    return Collections.emptySet();
  }

  public static SpringFactoryBeansManager getInstance() {
    return ServiceManager.getService(SpringFactoryBeansManager.class);
  }

  public void registerFactory(String className, ObjectTypeResolver reslover) {
    mySpringFactories.put(className, reslover);
  }

  public void unregisterFactory(String className) {
    mySpringFactories.remove(className);
  }

  private static class FactoryBeanObjectTypeReader extends ClassVisitor {
    private String myResultQName;

    public FactoryBeanObjectTypeReader() {
      super(Opcodes.API_VERSION);
    }

    public String getResultQName() {
      return myResultQName;
    }

    public MethodVisitor visitMethod(int access, @NonNls String name, String desc, String signature,
                                     String[] exceptions) {
      if ("getObjectType".equals(name) && (signature == null || signature.startsWith("()"))) {
        return new MethodVisitor(Opcodes.API_VERSION) {
          private String qname;
          private int number = 0;

          @Override
          public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if ((number == 0 || number == 7) && opcode == Opcodes.GETSTATIC || number == 5 && opcode == Opcodes.PUTSTATIC) {
              number++;
            }

          }

          @Override
          public void visitJumpInsn(int opcode, Label label) {
            if (number == 1 && opcode == Opcodes.IFNONNULL || number == 6 && opcode == Opcodes.GOTO) {
              number++;
            }
          }

          @Override
          public void visitLdcInsn(Object cst) {
            if (number == 2 && cst instanceof String) {
              number++;
              qname = (String)cst;
            }
            else if (number == 0 && cst instanceof Type) {
              number++;
              qname = ((Type)cst).getClassName();
            }
          }

          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (number != 3 || opcode != Opcodes.INVOKESTATIC || !"class$".equals(name)) return;
            number++;
          }

          @Override
          public void visitInsn(int opcode) {
            if (number == 4 && opcode == Opcodes.DUP) {
              number++;
            }
            if ((number == 8 || number == 1) && opcode == Opcodes.ARETURN) {
              if (myResultQName == null) {
                myResultQName = qname;
              }
              number++;
            }
          }

        };
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }
}
