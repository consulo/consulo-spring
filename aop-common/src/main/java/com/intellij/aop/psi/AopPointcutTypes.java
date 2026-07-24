/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.aop.localize.AopLocalize;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PathPattern;
import consulo.language.pratt.PrattBuilder;
import consulo.language.pratt.ReducingParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.*;

/**
 * @author peter
 */
public class AopPointcutTypes {
  private static final Map<String, AopElementType> ourPointcutTokens = new HashMap<>();
  private static final Map<String, PointcutDescriptor> ourPointcutDescriptors = new HashMap<>();

  public static Map<String, AopElementType> getPointcutTokens() {
    return ourPointcutTokens;
  }

  public static boolean canContainModifiers(String tokenText) {
    PointcutDescriptor descriptor = ourPointcutDescriptors.get(tokenText);
    return descriptor instanceof MethodPointcutDescriptor || descriptor instanceof FieldPointcutDescriptor;
  }

  static {
    registerPointcut(new MethodPointcutDescriptor("execution", false) {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiExecutionExpression(node);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("call", false) {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiCallExpression(node);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new MethodPointcutDescriptor("withincode", false) {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiWithinCodePointcutExpression(node);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("initialization", true) {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiInitializationPointcutExpression(node, false);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new MethodPointcutDescriptor("preinitialization", true) {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiInitializationPointcutExpression(node, true);
      }
    });

    registerPointcut(new FieldPointcutDescriptor("get") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new FieldPatternPointcut(node);
      }
    });
    registerPointcut(new FieldPointcutDescriptor("set") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new FieldPatternPointcut(node);
      }
    });

    registerPointcut(new PointcutDescriptor("args") {
      @Override
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
          parseParameterList(builder, TYPE_PATTERN, AopLocalize.errorMethodArgsPatternExpected().get());
          builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
        }
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiArgsExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("@args") {
      @Override
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
          parseParameterList(builder, SIMPLE_TYPE, AopLocalize.errorMethodAnnotationNameExpected().get());
          builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
        }
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtArgsExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("within") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiWithinExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("this") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiThisExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("target") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiTargetExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("handler") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiHandlerPointcutExpression(node);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new TypePatternPointcutDescriptor("staticinitialization") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiStaticInitializationExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@this") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtThisExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@target") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtTargetExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@within") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtWithinExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@annotation") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtAnnotationExpression(node);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new PointcutDescriptor("adviceexecution") {
      @Override
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get());
        builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAdviceExecutionExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("lock") {
      @Override
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get());
        builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiMonitorPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("unlock") {
      @Override
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get());
        builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiMonitorPointcutExpression(node);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new PointcutDescriptor("cflow") {
      @Override
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get());
        AopPrattParser.parsePointcut(builder, builder.createChildBuilder(0));
        builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiControlFlowPointcutExpression(node);
      }
    });
    //noinspection SpellCheckingInspection
    registerPointcut(new PointcutDescriptor("cflowbelow") {
      @Override
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get());
        AopPrattParser.parsePointcut(builder, builder.createChildBuilder(0));
        builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiControlFlowPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("if") {
      @Override
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
          builder.checkToken(AOP_BOOLEAN_LITERAL);
          builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
        }
      }

      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiIfPointcutExpression(node);
      }
    });


    Collection<AopElementType> types = ourPointcutTokens.values();
  }

  private static abstract class TypePatternPointcutDescriptor extends PointcutDescriptor {
    protected TypePatternPointcutDescriptor(String tokenText) {
      super(tokenText);
    }

    @Override
    public void parseToken(PrattBuilder builder) {
      parseTypePatternPointcut(builder, TYPE_PATTERN, AopLocalize.errorTypeNamePatternExpected().get());
    }
  }

  private static abstract class AnnoPatternPointcutDescriptor extends PointcutDescriptor {
    protected AnnoPatternPointcutDescriptor(String tokenText) {
      super(tokenText);
    }

    @Override
    public void parseToken(PrattBuilder builder) {
      parseTypePatternPointcut(builder, SIMPLE_TYPE, AopLocalize.errorMethodAnnotationNameExpected().get());
    }
  }

  private static void parseTypePatternPointcut(PrattBuilder builder, int level, String message) {
    if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
      MutableMarker type = builder.mark();
      builder.parseChildren(level, message);
      type.finish(AOP_REFERENCE_HOLDER);
      builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
    }
  }

  public static void parseParameterList(PrattBuilder builder, int level, String expectedMessage) {
    MutableMarker paramList = builder.mark();
    if (!builder.isToken(AOP_RIGHT_PAR)) {
      boolean allowDotDot = true;
      while (true) {
        if (allowDotDot && builder.checkToken(AOP_DOT_DOT)) {
          allowDotDot = false;
        }
        else {
          MutableMarker param = builder.mark();
          builder.parseChildren(level, expectedMessage);
          param.finish(AOP_REFERENCE_HOLDER);
          allowDotDot = true;
        }
        if (!builder.checkToken(AOP_COMMA)) break;
      }
    }
    paramList.finish(AOP_PARAMETER_LIST);
  }

  public static void registerPointcut(final PointcutDescriptor descriptor) {
    String token = descriptor.getTokenText();
    final String typeName = "AOP_" + (token.startsWith("@") ? "AT_" + token.substring(1).toUpperCase() : token.toUpperCase());
    AopElementType tokenType = new AopPointcutDesignatorTokenType(typeName);
    ourPointcutTokens.put(token, tokenType);
    ourPointcutDescriptors.put(token, descriptor);
    final AopPointcutElementType directiveType = new AopPointcutElementType(typeName + "_POINTCUT") {
      @Override
      public PsiPointcutExpression createPsi(ASTNode node) {
        return descriptor.createPsi(node);
      }
    };
    AopPrattParser.ourPrattRegistry.registerParser(tokenType, POINTCUT, PathPattern.path().up(), new ReducingParser() {
      @Override
      public IElementType parseFurther(PrattBuilder builder) {
        descriptor.parseToken(builder);
        return directiveType;
      }
    });
  }

  private AopPointcutTypes() {
  }
}
