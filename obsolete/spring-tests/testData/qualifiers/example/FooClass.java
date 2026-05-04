package example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;

@Service
@QualifierAnnotatedChild("fooServiceQ_1")
public class FooService1 {}

@Service
@ChildQualifierAnnotated
public class FooService2 {}

@Service
@QualifierAnnotated("fooServiceQ_3")
public class FooService3 {}

@Service
@QualifierAnnotated
public class FooService4 {}

@Service
@Qualifier
public class FooService5 {}

@Service
@Qualifier("fooServiceQ_6")
public class FooService6 {}


@Component
@ChildGenre("fooComponentQ_1")
public class FooComponent1 {}

@Component
@ChildGenre
public class FooComponent2 {}

@Repository
@Genre("fooRepositoryQ_1")
public class FooRepository1 {}

@Repository
@Genre
public class FooRepository2 {}

@Controller
@Qualifier
public class FooController1 {}

@Controller
@Qualifier("fooControllerQ_2")
public class FooController2 {}

}