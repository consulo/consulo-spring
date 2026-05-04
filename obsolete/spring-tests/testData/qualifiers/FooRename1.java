import org.springframework.beans.factory.annotation.*;
public class FooRename1 {
    @Autowired
    @Qualifier("f<caret>1")
    private FooInjection injection;
}