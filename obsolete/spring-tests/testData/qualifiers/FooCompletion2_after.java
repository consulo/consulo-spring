import org.springframework.beans.factory.annotation.*;
public class FooCompletion2 {
    @Autowired
    public void setInjection(@QualifierAnnotated("f2") FooInjection foo) {};
}