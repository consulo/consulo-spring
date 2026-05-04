package components;


@org.springframework.stereotype.Component()
public class ResourceAutowiredMethod {

  @javax.annotation.Resource()
  public void setS(String s) {}
  
  @javax.annotation.Resource()
  public void <error descr="Could not autowire. There are more than one bean of 'String' type. Beans: java.lang.String,s.">setString</error>(String s) {};

  @javax.annotation.Resource(name="<error descr="Cannot resolve bean 'unknown'">unknown</error>")
  public void setStr(String s) {}

  @javax.annotation.Resource(name="<error descr="Could not autowire. Bean should be of 'java.lang.String' type">boo</error>")
  public void setStr2(String s) {}
}
