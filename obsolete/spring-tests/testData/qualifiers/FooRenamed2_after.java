import org.springframework.beans.factory.annotation.*;
public class FooRenamed2 {
    @Autowired
    public void setInjection(@QualifierAnnotated("f2_new") FooInjection foo) {};
}