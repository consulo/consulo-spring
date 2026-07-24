package com.intellij.aop.parser;

import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionFileType;
import consulo.ide.impl.idea.openapi.application.PathManager;
import consulo.language.impl.DebugUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public class AopParsingTest extends LightCodeInsightFixtureTestCase {

  public void testExecution1() throws Throwable {
    doTest("execution (void transfer(..))");
  }

  public void testExecution2() throws Throwable {
    doTest("execution(public final !synchronized * *(..))");
  }

  public void testExecution3() throws Throwable {
    doTest("execution(* com.xyz.within.serv*.*.*(..))");
  }

  public void testExecution4() throws Throwable {
    doTest("execution(*||A set*A*b(..) throws (!com..* && * || (not A)), java.lang.*, !Object)");
  }

  public void testExecution5() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*b(..,com.*+[][],..,com..*,..))");
  }

  public void testExecution6() throws Throwable {
    doTest("execution");
  }

  public void testExecution7() throws Throwable {
    doTest("execution(");
  }

  public void testExecution8() throws Throwable {
    doTest("execution(*");
  }
  public void testExecution9() throws Throwable {
    doTest("execution(* com");
  }

  public void testExecution10() throws Throwable {
    doTest("execution(* com.");
  }

  public void testExecution11() throws Throwable {
    doTest("execution(* com.xyz.within.service.*");
  }

  public void testExecution12() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a");
  }

  public void testExecution13() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*");
  }

  public void testExecution14() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(");
  }

  public void testExecution15() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(..");
  }

  public void testExecution16() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(com..");
  }

  public void testExecution17() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(com..,");
  }

  public void testExecution18() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(,com..,");
  }

  public void testExecution19() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(*,..");
  }

  public void testExecution20() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(*,..");
  }

  public void testExecution21() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.a*(*,..)");
  }

  public void testExecution22() throws Throwable {
    doTest("execution(* *())");
  }

  public void testExecution23() throws Throwable {
    doTest("execution(* *() aaa.bbb");
  }

  public void testExecution24() throws Throwable {
    doTest("execution(int[] *())");
  }

  public void testExecution25() throws Throwable {
    doTest("execution(java.lang.Object[] *())");
  }

  public void testExecution26() throws Throwable {
    doTest("execution(int java.lang.Object.*(..,long...,..) throws *)");
  }

  public void testExecution27() throws Throwable {
    doTest("execution(* abc.MyService+.bar(Object+))");
  }

  public void testExecution28() throws Throwable {
    doTest("execution(xx+ abc.MyService+.bar(..))");
  }

  public void testExecution29() throws Throwable {
    doTest("execution(* abc.*Impl.foo(..))");
  }

  public void testExecution30() throws Throwable {
    doTest("execution(public * com.a.services.Serv.*(..)) or execution(public * com.b..*(..))");
  }

  public void testExecution31() throws Throwable {
    doTest("execution(* (newImpl.GTest || aaa.bbb).*(.., (Integer... && String)))");
  }

  public void testExecution32() throws Throwable {
    doTest("execution(!public * abc())");
  }

  public void testDoubleNegation() throws Throwable {
    doTest("!!@annotation(utils.MyAnno1)");
  }

  public void testAnnotations0() throws Throwable {
    doTest("execution(@foo.bar.aop.Marker public * foo.bar.aop.*.*(..))");
  }

  public void testAnnotations1() throws Throwable {
    doTest("execution(@NotNull @NonNls public (@foo.bar.aop.Immutable *) foo.bar.aop.*.*((!@Persistent @(NotNull || NonNls) @(org.xyz..*) *)))");
  }

  public void testAnnotations2() throws Throwable {
    doTest("within(@(@Inherited *) org.xyz..*)");
  }

  public void testAnnotations3() throws Throwable {
    doTest("execution(public (@utils.* *) *(..))");
  }

  public void testAnnotations3_5() throws Throwable {
    doTest("execution(public (@(utils..*) *) *(..))");
  }

  public void testAnnotations4() throws Throwable {
    doTest("execution(@utils.* * *(@utils.MyNewTypeAnno *,..))");
  }

  public void testAnnotations5() throws Throwable {
    doTest("execution(* @javax.jws.WebService *.*(..))");
  }

  public void testAnnotations6() throws Throwable {
    doTest("execution(public @(*..Foo || *..Bar) * *(..))");
  }

  public void testAnnotations7() throws Throwable {
    doTest("execution(@FooAnno !@BarAnno * *(..))");
  }
  
  public void testAnnotations8() throws Throwable {
    doTest("within(@FooAnno !@BarAnno *)");
  }

  public void testAnnotationParams1() throws Throwable {
    doTest("within(@FooAnno(a=b, c.d.e={@Smth}) !@BarAnno *)");
  }

  public void testAnnotationParams2() throws Throwable {
    doTest("within(@FooAnno(a=b, c.d.e={@Smth}) !@BarAnno (C || A))");
  }

  public void testAnnotationParams3() throws Throwable {
    doTest("within(@FooAnno() *)");
  }

  public void testGenerics1() throws Throwable {
    doTest("execution(java.util.List<? extends java.lang.Object,java.util.List<? super java..*>,*> set*(List<?>, List<? super T>, List<T>, ..))");
  }

  public void testGenerics2() throws Throwable {
    doTest("execution(* *(List<Object>, Object<List>[]))");
  }

  public void testWithin1() throws Throwable {
    doTest("within(com.xyz.someapp.trading..*)");
  }

  public void testWithin2() throws Throwable {
    doTest("within(com.execution.service.*)");
  }

  public void testWithin3() throws Throwable {
    doTest("within");
  }

  public void testWithin4() throws Throwable {
    doTest("within(");
  }

  public void testWithin5() throws Throwable {
    doTest("within(..");
  }

  public void testWithin6() throws Throwable {
    doTest("within(..)");
  }

  public void testWithin7() throws Throwable {
    doTest("within(a<>)");
  }

  public void testReference1() throws Throwable {
    doTest("anyPublicOperation() && !inTrading()");
  }

  public void testReference2() throws Throwable {
    doTest("com.xyz.within.SystemArchitecture.businessService()");
  }

  public void testReference3() throws Throwable {
    doTest("(anyPublicOperation()) and not (inTrading()) or inTrading()");
  }

  public void testReference4() throws Throwable {
    doTest("(anyPublicOperation");
  }

  public void testReference5() throws Throwable {
    doTest("(anyPublicOperation(");
  }

  public void testReference6() throws Throwable {
    doTest("(anyPublicOperation()");
  }

  public void testReference7() throws Throwable {
    doTest("(anyPublicOperation())");
  }

  public void testReference8() throws Throwable {
    doTest("(anyPublicOperation()) and ");
  }

  public void testReference9() throws Throwable {
    doTest("(anyPublicOperation()) and not ");
  }

  public void testReference10() throws Throwable {
    doTest("(anyPublicOperation()) and not (");
  }

  public void testReference11() throws Throwable {
    doTest("(anyPublicOperation()) and not (inTrading()) or");
  }

  public void testReference12() throws Throwable {
    doTest("anyPublicOperation() and inTrading() or inTrading()");
  }

  public void testReference13() throws Throwable {
    doTest("(anyPublicOperation()) && ! (inTrading()) || inTrading()");
  }

  public void testReference14() throws Throwable {
    doTest("execution.ref()");
  }

  public void testReference15() throws Throwable {
    doTest("execution.");
  }

  public void testReference16() throws Throwable {
    doTest("com.xyz.within.SystemArchitecture.businessService(aaa,bbb,ccc.ddd,long)");
  }

  public void testThis1() throws Throwable {
    doTest("this(com.xyz.service.AccountService[])");
  }

  public void testThis2() throws Throwable {
    doTest("this(");
  }

  public void testThis3() throws Throwable {
    doTest("this(com.xyz.service.AccountService[)");
  }

  public void testThis4() throws Throwable {
    doTest("this(a<>)");
  }

  public void testTarget1() throws Throwable {
    doTest("target(com.xyz.service.AccountService)");
  }

  public void testTarget2() throws Throwable {
    doTest("target(");
  }

  public void testTarget3() throws Throwable {
    doTest("target(a<>)");
  }

  public void testArgs1() throws Throwable {
    doTest("args(java.io.Serializable)");
  }

  public void testArgs2() throws Throwable {
    doTest("args(..,java.io.Serializable)");
  }

  public void testArgs3() throws Throwable {
    doTest("args(..,*,java.io.Serializable)");
  }

  public void testArgs4() throws Throwable {
    doTest("args(..,*,java.io..*)");
  }

  public void testArgs5() throws Throwable {
    doTest("args");
  }

  public void testArgs6() throws Throwable {
    doTest("args(");
  }

  public void testArgs7() throws Throwable {
    doTest("args(..");
  }

  public void testArgs8() throws Throwable {
    doTest("args(..,");
  }

  public void testArgs9() throws Throwable {
    doTest("args(..,*");
  }

  public void testArgs10() throws Throwable {
    doTest("args(..,*,java.io..");
  }

  public void testArgs11() throws Throwable {
    doTest("args(java.io.* || int)");
  }

  public void testArgs12() throws Throwable {
    doTest("args(foo<bar,?,? extends foo, ? super bar>)");
  }

  public void testEmpty() throws Throwable {
    doTest("");
  }

  public void testBad() throws Throwable {
    doTest("%@%^$^*$*^%#(*^T^(*^&*% %^*$C )%^ ()@#T*^!R &)*O R^*( &T@#IHEUOIWsgvfdsjrtg ltgnethwerndngfnrwe89p u1b;i1ehaa");
  }

  public void testAtTarget1() throws Throwable {
    doTest("@target(org.springframework.transaction.args.Transactional)");
  }

  public void testAtTarget2() throws Throwable {
    doTest("@target(");
  }

  public void testAtThis1() throws Throwable {
    doTest("@this(org.springframework.transaction.args.Transactional)");
  }

  public void testAtThis2() throws Throwable {
    doTest("@this(");
  }

  public void testAtWithin1() throws Throwable {
    doTest("@within(org.springframework.transaction.annotation.Transactional)");
  }

  public void testAtWithin2() throws Throwable {
    doTest("@within(");
  }

  public void testAtAnnotation1() throws Throwable {
    doTest("@annotation(org.springframework.transaction.annotation.Transactional)");
  }

  public void testAtAnnotation2() throws Throwable {
    doTest("@annotation(");
  }

  public void testAtArgs1() throws Throwable {
    doTest("@args(com.xyz.security.Classified)");
  }

  public void testAtArgs2() throws Throwable {
    doTest("@args(");
  }

  public void testCallMethod1() throws Throwable {
    doTest("call(* *())");
  }

  public void testCallConstructor1() throws Throwable {
    doTest("call(*.new())");
  }

  public void testInitialization1() throws Throwable {
    doTest("initialization(*.new())");
  }

  public void testInitialization2() throws Throwable {
    doTest("initialization(*.newa())");
  }

  public void testPreinitialization1() throws Throwable {
    doTest("preinitialization(*.new())");
  }

  public void testPreinitialization2() throws Throwable {
    doTest("preinitialization(*.newa())");
  }

  public void testPreinitialization3() throws Throwable {
    doTest("preinitialization(");
  }

  public void testWithincode1() throws Throwable {
    doTest("withincode(* *.newa())");
  }

  public void testWithincode2() throws Throwable {
    doTest("withincode(*.new())");
  }

  public void testStaticinitialization1() throws Throwable {
    doTest("staticinitialization(*.A+)");
  }

  public void testHandler1() throws Throwable {
    doTest("handler(*.A+)");
  }
  
  public void testAdviceexecution1() throws Throwable {
    doTest("adviceexecution()");
  }

  public void testSet1() throws Throwable {
    doTest("set(static int T.x)");
  }

  public void testGet1() throws Throwable {
    doTest("get(!static * com..*.foo)");
  }

  public void testLock1() throws Throwable {
    doTest("lock()");
  }
  
  public void testUnlock1() throws Throwable {
    doTest("unlock()");
  }

  public void testCflow1() throws Throwable {
    doTest("cflow(args())");
  }
  
  public void testCflow2() throws Throwable {
    doTest("cflow(");
  }

  public void testCflowbelow1() throws Throwable {
    doTest("cflowbelow(args() && this(a))");
  }

  public void testIf1() throws Throwable {
    doTest("if(true) || if(false) && if() && if(");
  }

  public void testPointcutNameAsId() throws Throwable {
    doTest("execution(* Foo.get(..))");
  }

  private void doTest(@NonNls String code) {
    AopPointcutExpressionFile psiFile = parsePointcutExpression(code);
    String tree = DebugUtil.psiTreeToString(psiFile, true);
    String path = PathManager.getHomePath() + "/svnPlugins/aop-common/tests/com/intellij/aop/parser/data/" + getTestName(true) + ".txt";
    assertSameLinesWithFile(path, tree);
  }

  public AopPointcutExpressionFile parsePointcutExpression(String code) {
    return (AopPointcutExpressionFile)createLightFile(AopPointcutExpressionFileType.INSTANCE, code);
  }

}
