import java.util.*;

public class FooBeanWithConstructors3 {

  public static class BooleanConstructor {
    public BooleanConstructor(boolean foo) {}
  }

  public static class ByteConstructor {
   public ByteConstructor(byte foo) {}
 }

  public static class CharConstructor {
    public CharConstructor(char foo) {}
  }

  public static class DoubleConstructor {
    public DoubleConstructor(double foo) {}
  }

  public static class FloatConstructor {
    public FloatConstructor(float foo) {}
  }

  public static class IntConstructor {
    public IntConstructor(int foo) {}
  }

  public static class LongConstructor {
    public LongConstructor(long foo) {}
  }

  public static class ShortConstructor {
    public ShortConstructor(short foo) {}
  }

  public static class StringConstructor {
    public StringConstructor(String foo) {}
  } 

  public FooBeanWithConstructors3 (StringConstructor foo) {}

  //****************************************
   public FooBean getBoolean(boolean foo) {return null;}

   public FooBean getByte(byte foo) {return null;}

   public FooBean getChar(char foo) {return null;}

   public FooBean getDouble(double foo) {return null;}

   public FooBean getFloat(float foo) {return null;}

   public FooBean getInt(int foo) {return null;}

   public FooBean getLong(long foo) {return null;}

   public FooBean getShort(short foo) {return null;}

   public FooBean getString(String foo) {return null;}

   public static FooBean getInstance(String foo) {return null;}
}