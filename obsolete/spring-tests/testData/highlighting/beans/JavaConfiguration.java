package beans;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Configuration;

@Configuration
public abstract class  JavaConfiguration  {
  @Bean(aliases = {"fooBean_Alias", "fooBean_Alias2"})
  public FooBean fooBean() {
    return new FooBean();
  }

  @Bean(aliases = {"fooBean2_Alias", "alias"})
  public FooBean2 fooBean2() {
    FooBean2 fooBean2 = new FooBean2();
    fooBean2.setyFooBean3(fooBean3());
    return fooBean2;
  }

  @Bean()
  public FooBean3 fooBean3() {
    return new FooBean3();
  }

  @Bean()
  private FooBean5 privateBean() {
    return null;
  }
  @Bean()
  protected FooBean5 protectedBean() {
    return null;
  }

  @Bean()
  FooBean5 packageBean() {
    return null;
  }

  @Bean()
  public FooBean5 publicBean() {
    return null;
  }

  @ExternalBean()
  public abstract FooBean externalBean();
}
