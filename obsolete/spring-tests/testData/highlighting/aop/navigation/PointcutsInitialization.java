package foo.bar.testcase;

import org.aspectj.lang.annotation.Pointcut;

class Pointcuts {
    @Pointcut("execution(* foo.bar.testcase.Target.*(..))")
    public void fooFoo() {}
}

class Target {
    void foo() {}
}