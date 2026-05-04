package components;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.*;
import components.*;

@Service
@ChildGenre("fooServiceChildGenreQualifier")
public class FooService1 {}

@Service
@ChildGenre
public class FooService2 {}

@Service("fooServiceGenre")
@Genre("fooServiceGenreQualifier")
public class FooService3 {}

@Service
@Genre
public class FooService4 {}

@Service
@Qualifier
public class FooService5 {}

@Service
@Qualifier("fooServiceQualifier")
public class FooService6 {}


@Component
@ChildGenre("fooComponentChildGenre")
public class FooComponent1 {}

@Component
@ChildGenre
public class FooComponent2 {}

@Repository("fooRepositoryGenre")
@Genre("fooRepositoryGenreQualifier")
public class FooRepository1 {}

@Repository
@Genre
public class FooRepository2 {}

@Controller
@Qualifier
public class FooController1 {}

@Controller
@Qualifier("fooControllerQualifier")
public class FooController2 {}

}