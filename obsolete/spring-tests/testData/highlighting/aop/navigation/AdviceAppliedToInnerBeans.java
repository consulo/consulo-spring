package foo.bar.testcase;

public class TargetA {
    public void methodA(){}
}
public class TargetB {
    public void methodB(){}
}
public class Wrapper {
    public void setTarget(TargetB target) {
    }
}
public class AspectBean {
    public void before(JoinPoint jp) throws Throwable {
    }
}