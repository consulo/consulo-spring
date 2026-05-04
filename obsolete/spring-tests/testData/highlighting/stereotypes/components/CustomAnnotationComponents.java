package components;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;
import components.*;

  @CustomComponentAnnotation
  public class FooComponent1 {}

  @CustomComponentAnnotation("fooComponent_2")
  public class FooComponent2 {}

  @CustomComponentAnnotation
  @Qualifier("fooQualifiedComponent")
  public class FooComponent3 {}

  @CustomComponentAnnotation
  @Genre
  public class FooComponent4 {}

  @CustomComponentAnnotation
  @ChildGenre("fooComponentChildGenre")
  public class FooComponent5 {}

  @CustomComponentAnnotationChild
  @ChildGenre
  public class FooComponent6 {}

  @CustomComponentAnnotationChild("fooComponent_7")
  @Qualifier("fooQualifiedComponent2")
  public class FooComponent7 {}

}