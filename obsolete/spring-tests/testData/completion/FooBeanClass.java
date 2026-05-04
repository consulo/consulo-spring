import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FooBeanClass {
    public FooBeanClass() {
       Resource is = new FileSystemResource("spring-beans.xml");
       BeanFactory factory = new XmlBeanFactory(is);

       factory.getBean("foo<caret>");
    }
}
