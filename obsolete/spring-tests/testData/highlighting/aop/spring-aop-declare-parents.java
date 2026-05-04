public class Target {}

public interface Mixin {}

public class MixinImpl implements Mixin {}

public class DummyAspect {}

public class Other {
   public void setTarget(Mixin mixin) {}
}