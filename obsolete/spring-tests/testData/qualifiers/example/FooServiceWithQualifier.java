package example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;

@Service
@Qualifier("fooQua<caret>lifier")
public class FooServiceWithQualifier {}
