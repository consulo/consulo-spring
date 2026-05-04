package components;


@org.springframework.stereotype.Component()
public class ResourceAutowiredField {

  @javax.annotation.Resource(name="s")
  private String st<caret>r;
  
  @javax.annotation.Resource()
  private String s;

  @javax.annotation.Resource()
  private String <error descr="Could not autowire. There are more than one bean of 'String' type. Beans: java.lang.String,s.">badstr</error>;

  @javax.annotation.Resource(name="<error descr="Cannot resolve bean 'unknown'">unknown</error>")
  private String badstr1;

  @javax.annotation.Resource(name="<error descr="Could not autowire. Bean should be of 'java.lang.String' type">boo</error>")
  private String badstr2;
}
