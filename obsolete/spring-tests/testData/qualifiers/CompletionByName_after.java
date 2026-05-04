import org.springframework.beans.factory.annotation.*;
public class CompletionByName {
    @Autowired
    @Qualifier("byName")
    private FooInjection injection;
}