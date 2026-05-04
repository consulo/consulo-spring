package example;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import components.*;

@Service
public class FooAutowiredService {

     @Autowired
     public void inject(@Qualifier("fooQualifier_new") FooServiceWithQualifier foo) {};

}
