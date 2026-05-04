import java.util.*;

public class FooBeanWithConstructors4 {

  public FooBeanWithConstructors4(FooBean foo1, FooBean3 foo2, int a) {}

  public FooBeanWithConstructors4(FooBean foo1, FooBean3 foo2) {}

  public static FooBeanWithConstructors4 getInstance(FooBean foo1, FooBean3 foo2) {return null;}
  public static FooBeanWithConstructors4 getInstance(int a, FooBean foo1, FooBean3 foo2) {return null;}
}