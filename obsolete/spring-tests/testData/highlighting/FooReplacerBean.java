import java.lang.reflect.Method;

public class FooReplacerBean implements org.springframework.beans.factory.support.MethodReplacer {
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        return null;
    }
}