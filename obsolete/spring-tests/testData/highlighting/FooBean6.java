import org.hibernate.SessionFactory;

public abstract class FooBean6 {
    public FooBean6(SessionFactory aaa) {
    }

    //must be included
    public void destroy_1(){}
    protected void destroy_2(){}
    private void destroy_3() {}
    void destroy_4(){}
    public void destroy_5(boolean b) {}

    // not included
    public void destroy_6(String b) {}
    public void destroy_7(String b, String b2){}
    public abstract void destroy_8(){}


    //must be included
    public void init_1() {}
    protected void init_2() {}
    private void init_3(){}

    //not included
    private void init_4(boolean b) {}

}