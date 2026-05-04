package example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;

@Service
@Qualifier("fooQualifier_new")
public class FooServiceWithQualifier {}
