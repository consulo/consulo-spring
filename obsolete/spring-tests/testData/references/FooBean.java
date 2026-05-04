import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FooBean {
    public FooBean() {
       Resource is = new FileSystemResource("applicationContext.xml");
       BeanFactory factory = new XmlBeanFactory(is);

       factory.getBean("foo");
    }
}
