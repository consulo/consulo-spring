public abstract class FooLookupBean {
    protected abstract FooBean createCommand();

    private FooBean createCommand1() {return null;}

    public abstract FooBean createCommand2();
    public FooBean createCommand3() { return null;}

    protected abstract FooBean createCommand4();

    protected abstract FooBean createCommand5(String foo);

    protected abstract FooBean3 createCommand7();
    
    protected static FooBean createCommandStatic() { return null;}
}
