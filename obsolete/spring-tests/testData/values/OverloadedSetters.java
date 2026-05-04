import java.lang.String;

public class OverloadedSetters {

  public void setProp(String value) {}
  public void setProp(boolean value) {}
  public void setProp(int value) {}

  public void setProp2(boolean value) {}
  public void setProp2(int value) {}
}