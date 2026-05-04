package example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("test")
public class QualifierWithoutValue {

    @Autowired
    public void setMer(<error>@Qualifier()</error> QualifierWithoutValue boo) {}
}