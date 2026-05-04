import org.aspectj.lang.annotation.*;

@Aspect
public class MyAspect {

    @Before("this(MyAspect)")
    public void before() {}

    @After("this(MyAspect)")
    public void after() {}

    @Around(value = "this(MyAspect)")
    public Object around() { return null; }

    @AfterThrowing
    public void afterThrowing() {}

    @AfterReturning
    public void afterReturning() {}

  public void <warning descr="Method 'unused()' is never used">unused</warning>() {}
}
