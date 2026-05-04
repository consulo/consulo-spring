import org.springframework.beans.factory.annotation.*;
public class FooRename1 {
    @Autowired
    @Qualifier("f1_new")
    private FooInjection injection;
}