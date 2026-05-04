package beans;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Configuration;

import java.lang.String;

@Configuration
public abstract class  JavaConfig {
  @Bean(aliases = {"javaConfiguredBeanAlias", "javaConfiguredBeanAlias2"})
  public String javaConfiguredBean() {
    return "";
  }

  @Bean()
  private String javaConfiguredPrivateBean() {
    return null;
  }
  @Bean()
  protected String javaConfiguredProtectedBean() {
    return null;
  }
}
