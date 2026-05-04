package foo.bar;

@org.springframework.stereotype.Component(name="abc")
public class Bean {
  public void foo() {}
}

public class NonBean {
  public void foo() {}
}