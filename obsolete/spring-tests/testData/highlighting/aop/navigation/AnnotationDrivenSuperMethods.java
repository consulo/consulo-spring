@org.springframework.transaction.annotation.Transactional
interface Intf {
  public void foo() {}
}

class Bean implements Intf {
  public void foo() {}
  private void bar() {}
}

class Super {
  @org.springframework.transaction.annotation.Transactional
  public void fromSuper() {}
}

class Sub extends Super {
  public void fromSuper() {}  
}