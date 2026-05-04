import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

public class Bean {
  public void foo() {}
}

public class XmlAspectBean {
}