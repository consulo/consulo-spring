public class FactoryBean {
    public static FactoryBean getInstance() {
        return new FactoryBean();
    }
    public String getInstanceBadReturnType() {
        return new FactoryBean();
    }
    public FactoryBean getInstanceNonStatic() {
        return new FactoryBean();
    }
    public FooBean createFooBean() {
        return new FooBean();
    }
    public FooBean2 createFooBean2() {
        return new FooBean2();
    }

    public FooBeanWithConstructors2 createFooBeanWithConstructors2(String foo) {
        return new FooBean2();
    }

    public FooBean3 createFooBean3() {
        return new FooBean3();
    }

    public String createFooBeanBadReturnType() {
        return new FooBean();
    }
    public FooBean createFooBeanNonStatic() {
        return new FooBean();
    }
}