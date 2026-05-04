import org.springframework.beans.factory.annotation.*;
public class FooRenamed2 {
    @Autowired
    public void setInjection(@QualifierAnnotated("<caret>f2") FooInjection foo) {};
}