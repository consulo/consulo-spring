import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Configuration;

@Configuration
public abstract class  JavaConfigExternalBeansRename1 {

  @ExternalBean()
  public abstract FooBean externalBean();
}
