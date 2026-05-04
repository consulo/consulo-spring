import org.aspectj.lang.annotation.*;


@Aspect
public class MyAspect {

  @AfterReturning(value = "execution(* Foo.perform(..))", returning = "value")
  public Object bar(Object va<caret>lue) throws Throwable {
    return null;
  }

}
