import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Configuration;

@Configuration
public abstract class  JavaConfigExternalBeans {

  @ExternalBean()
  public abstract FooBean <caret>externalBean();

  @ExternalBean()
  public abstract String <warning>unknownBean</warning>();

  @ExternalBean()
  public abstract <warning>int</warning> externalBean2();
}
