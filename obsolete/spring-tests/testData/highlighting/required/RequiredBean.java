public class RequiredBean {

  @org.springframework.beans.factory.annotation.Required
  public void <error>setProp</error>(String prop) {}

  @org.springframework.beans.factory.annotation.Required
  public void setAnotherProp(String prop) {}
}