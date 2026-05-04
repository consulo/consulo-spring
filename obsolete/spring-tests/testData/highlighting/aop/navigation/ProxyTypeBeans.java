package foo;

class AspectClass {}

interface TargetInterface {
  void interfaceMethod();
}

class TargetClassNoInterface {
  public void classMethod() {}
}

class TargetClassWithInterface implements TargetInterface {
  public void interfaceMethod() {}
  public void classMethod() {}
}