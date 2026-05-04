import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

public class Bean {
  @Transactional
  public void foo() {}
}

@Aspect
@Order(2)
public class AspectBean {
  @Before("execution(* foo())")
  void inAspect() {}
}

@Aspect
public class OrderedAspectBean implements Ordered {
  @Before("execution(* foo())")
  void inOrderedAspect() {}

  public int getOrder() {return 3;}
}

public class AdviceBean implements Advice {

}

public class XmlAspectBean {
  void before();
  void after();
}

public class NonBean {
  public void foo() {}
}