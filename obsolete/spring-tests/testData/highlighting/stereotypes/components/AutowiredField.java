package components;


import java.util.List;

@org.springframework.stereotype.Service()
public class AutowiredField {

  @org.springframework.beans.factory.annotation.Autowired
  private List<String> st<caret>rList;

  @org.springframework.beans.factory.annotation.Autowired
  private String[] strArray;

  @org.springframework.beans.factory.annotation.Autowired
  private String <error>str</error>;

  @org.springframework.beans.factory.annotation.Autowired
  private AutowiredField self;
}