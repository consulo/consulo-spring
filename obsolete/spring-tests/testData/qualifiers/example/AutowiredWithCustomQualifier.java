package example;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@CustomQualifier
@Component
public class AutowiredWithCustomQualifier {

  @Autowired
  @CustomQualifier
  private AutowiredWithCustomQualifier myGood;

  @Autowired
  <error>@CustomQualifier</error>
  private String myBad;
}