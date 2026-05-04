package com.intellij.spring.model.xml.custom;

import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;

public class LenientProblemReporter implements ProblemReporter {
  public void fatal(Problem problem) {
    throw new BeanDefinitionParsingException(problem);
  }

  public void error(Problem problem) {
    throw new BeanDefinitionParsingException(problem);
  }

  public void warning(Problem problem) {
  }
}
