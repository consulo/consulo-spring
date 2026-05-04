import org.springframework.beans.factory.annotation.*;
public class FooCompletion1 {
    @Autowired
    @Qualifier("f<caret>")
    private FooInjection injection;
}