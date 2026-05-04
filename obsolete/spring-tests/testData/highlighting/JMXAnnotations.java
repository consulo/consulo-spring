public class <warning descr="Class 'JMXAnnotations' is never used">JMXAnnotations</warning> {

    @org.springframework.jmx.export.annotation.ManagedOperation
    public void fooBenManaged() {
    }


    @org.springframework.jmx.export.annotation.ManagedAttribute
    public void getMe() {
    }

  public void <warning descr="Method 'unused()' is never used">unused</warning>() {}

}