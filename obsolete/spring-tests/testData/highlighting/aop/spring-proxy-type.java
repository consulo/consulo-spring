interface Intf {
  void foo();
}

public class Target implements Intf {
  void foo();
}

public class ClassTarget {
  void foo();
}

public class NonTarget implements Intf {
  void foo();
}

public class IntroTarget implements Intf {
  void foo();
}

public interface Mixin {}

public class MixinImpl implements Mixin {}

public class DummyAspect {
  public void xxx() {}
}

public class Other {
   public void setTargetClass(Target t) {}
   public void setTargetIntf(Intf t) {}
   public void setClassTarget(ClassTarget t) {}
   public void setIntroTarget(IntroTarget t) {}
   public void setNonTargetIntf(Intf t) {}
   public void setNonTargetClass(NonTarget t) {}

}