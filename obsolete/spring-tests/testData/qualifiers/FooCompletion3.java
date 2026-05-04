import org.springframework.beans.factory.annotation.*;
public class FooCompletion3 {
    @Autowired
    public void inject(FooService2 inj2, @QualifierAnnotatedChild("f<caret>") FooInjection inj) {};
}