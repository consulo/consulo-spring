/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopBundle;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PathPattern;
import consulo.language.pratt.PrattBuilder;
import consulo.language.pratt.ReducingParser;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.*;

/**
 * @author peter
 */
public class AopPointcutTypes {
  private static final Map<String, AopElementType> ourPointcutTokens = new HashMap<String, AopElementType>();
  private static final Map<String, PointcutDescriptor> ourPointcutDescriptors = new HashMap<String, PointcutDescriptor>();

  public static Map<String, AopElementType> getPointcutTokens() {
    return ourPointcutTokens;
  }

  public static boolean canContainModifiers(String tokenText) {
    PointcutDescriptor descriptor = ourPointcutDescriptors.get(tokenText);
    return descriptor instanceof MethodPointcutDescriptor || descriptor instanceof FieldPointcutDescriptor;
  }

  static {
    registerPointcut(new MethodPointcutDescriptor("execution", false) {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiExecutionExpression(node);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("call", false) {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiCallExpression(node);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("withincode", false) {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiWithinCodePointcutExpression(node);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("initialization", true) {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiInitializationPointcutExpression(node, false);
      }
    });
    registerPointcut(new MethodPointcutDescriptor("preinitialization", true) {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiInitializationPointcutExpression(node, true);
      }
    });

    registerPointcut(new FieldPointcutDescriptor("get") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new FieldPatternPointcut(node);
      }
    });
    registerPointcut(new FieldPointcutDescriptor("set") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new FieldPatternPointcut(node);
      }
    });


    registerPointcut(new PointcutDescriptor("args") {
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
          parseParameterList(builder, TYPE_PATTERN, AopBundle.message("error.method.args.pattern.expected"));
          builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        }
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiArgsExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("@args") {
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
          parseParameterList(builder, SIMPLE_TYPE, AopBundle.message("error.method.annotation.name.expected"));
          builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        }
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtArgsExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("within") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiWithinExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("this") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiThisExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("target") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiTargetExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("handler") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiHandlerPointcutExpression(node);
      }
    });
    registerPointcut(new TypePatternPointcutDescriptor("staticinitialization") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiStaticInitializationExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@this") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtThisExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@target") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtTargetExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@within") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtWithinExpression(node);
      }
    });
    registerPointcut(new AnnoPatternPointcutDescriptor("@annotation") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAtAnnotationExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("adviceexecution") {
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiAdviceExecutionExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("lock") {
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiMonitorPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("unlock") {
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiMonitorPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("cflow") {
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("));
        AopPrattParser.parsePointcut(builder, builder.createChildBuilder(0));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiControlFlowPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("cflowbelow") {
      public void parseToken(PrattBuilder builder) {
        builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("));
        AopPrattParser.parsePointcut(builder, builder.createChildBuilder(0));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiControlFlowPointcutExpression(node);
      }
    });
    registerPointcut(new PointcutDescriptor("if") {
      public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
          builder.checkToken(AOP_BOOLEAN_LITERAL);
          builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        }
      }

      public PsiPointcutExpression createPsi(ASTNode node) {
        return new PsiIfPointcutExpression(node);
      }
    });


    Collection<AopElementType> types = ourPointcutTokens.values();
  }

  private static abstract class TypePatternPointcutDescriptor extends PointcutDescriptor {
    protected TypePatternPointcutDescriptor(@NonNls String tokenText) {
      super(tokenText);
    }

    public void parseToken(PrattBuilder builder) {
      parseTypePatternPointcut(builder, TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
    }

  }

  private static abstract class AnnoPatternPointcutDescriptor extends PointcutDescriptor {
    protected AnnoPatternPointcutDescriptor(@NonNls String tokenText) {
      super(tokenText);
    }

    public void parseToken(PrattBuilder builder) {
      parseTypePatternPointcut(builder, SIMPLE_TYPE, AopBundle.message("error.method.annotation.name.expected"));
    }

  }

  private static void parseTypePatternPointcut(PrattBuilder builder, int level, String message) {
    if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
      MutableMarker type = builder.mark();
      builder.parseChildren(level, message);
      type.finish(AOP_REFERENCE_HOLDER);
      builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
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
    @NonNls final String typeName = "AOP_" + (token.startsWith("@") ? "AT_" + token.substring(1).toUpperCase() : token.toUpperCase());
    AopElementType tokenType = new AopPointcutDesignatorTokenType(typeName);
    ourPointcutTokens.put(token, tokenType);
    ourPointcutDescriptors.put(token, descriptor);
    final AopPointcutElementType directiveType = new AopPointcutElementType(typeName + "_POINTCUT") {
      public PsiPointcutExpression createPsi(ASTNode node) {
        return descriptor.createPsi(node);
      }
    };
    AopPrattParser.ourPrattRegistry.registerParser(tokenType, POINTCUT, PathPattern.path().up(), new ReducingParser() {
      public IElementType parseFurther(PrattBuilder builder) {
        descriptor.parseToken(builder);
        return directiveType;
      }
    });
  }

  private AopPointcutTypes() {
  }

}
