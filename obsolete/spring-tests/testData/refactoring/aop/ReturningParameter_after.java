import org.aspectj.lang.annotation.*;


@Aspect
public class MyAspect {

  @AfterReturning(value = "execution(* Foo.perform(..))", returning = "newName")
  public Object bar(Object ne<caret>wName) throws Throwable {
    return null;
  }

}
