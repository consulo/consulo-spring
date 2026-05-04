import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Configuration;

@Configuration
public abstract class  JavaConfigExternalBeans2 {

  @ExternalBean()
  public abstract FooBean externalBean();
}
