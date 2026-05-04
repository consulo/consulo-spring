public class MyApp {

    public enum MyEnum {
        CONSTANT
    }

    public MyApp(MyEnum myEnum) {
        System.out.println("myEnum = " + myEnum);
    }

    public static void main(String[] args) {
        new org.springframework.context.support.FileSystemXmlApplicationContext(args);
    }
}