package foo.bar;

@org.springframework.stereotype.Component("abc")
public class Bean {
  public void foo() {}
}

public class NonBean {
  public void foo() {}
}

@org.springframework.stereotype.Component("aspect1")
public class AspectBean {
}