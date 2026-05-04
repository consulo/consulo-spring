import org.springframework.beans.factory.annotation.*;
public class CompletionByName {
    @Autowired
    @Qualifier("by<caret>")
    private FooInjection injection;
}