import org.springframework.beans.factory.annotation.*;
import example.*;

public class Foo {
    @Autowired
    @Qualifier("f1")
    private FooInjection injection;

    @Autowired
    @Qualifier("<error>unknown</error>")
    private FooInjection injectionError1;

    @Autowired
    @Qualifier("foo")
    private Foo injectionByName;

    @Autowired // more than 1 bean of FooInjection type
    private FooInjection <error descr="Could not autowire. There are more than one bean of 'FooInjection' type. Beans: FooInjection,FooInjection,FooInjection,FooInjection.">injectionError2</error>;

    @Autowired
    @Qualifier("<error>f2</error>")
    private String injectionError3;

    @Autowired
    @Qualifier("f2")
    private FooInjection injection2;

    @Autowired
    @QualifierAnnotated("f3")
    private FooInjection injection3;

    @Autowired
    @QualifierAnnotatedChild("f4")
    private FooInjection injection4;

    @Autowired
    @QualifierAnnotatedChild("fooServiceQ_1")
    private FooService1 fooService1;
    
    @Autowired
    @QualifierAnnotated("fooServiceQ_3")
    private FooService3 f3;


    @Autowired
    @Qualifier("fooServiceQ_6")
    private FooService6 f6;

    @Autowired
    public void inject(@Qualifier("f1") FooInjection injection,
                       @Qualifier("f2") FooInjection injection2,
                       @QualifierAnnotated("f3") FooInjection injection3,
                       @QualifierAnnotatedChild("fooServiceQ_1") FooService1 serv1) { }

    @Autowired
    public void inject2(@Qualifier("<error>unknown</error>") FooInjection injection,
                        FooInjection <error>injection2</error>,
                       @QualifierAnnotated("<error>f3</error>") String injection3) { }
}