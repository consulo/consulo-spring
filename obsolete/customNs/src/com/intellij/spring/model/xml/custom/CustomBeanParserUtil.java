/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.custom;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

/**
 * @author peter
 */
public class CustomBeanParserUtil {
  static final String COPY_KEY = "CustomBeanParser.COPY_KEY";
  private static final int[] EMPTY_INT_ARRAY = new int[0];

  private CustomBeanParserUtil() {
  }

  static void parseCustomBean(final String tagText, int timeout) {
    List result;
    try {
      result = getAdditionalBeans(tagText, timeout);
    }
    catch (Throwable throwable) {
      CustomBeanParser.printException(throwable);
      return;
    }
    if (result == null) {
      System.out.print("timeout\n\n");
      return;
    }

    System.out.print("result\n");
    System.out.print(result.get(0) + "\n"); //has_infrastructures
    for (int i = 1; i < result.size(); i++) {
      List s2 = (List)result.get(i);
      System.out.print("info\n");
      for (int i1 = 0; i1 < s2.size(); i1++) {
        String s1 = (String)s2.get(i1);
        System.out.print(s1 + "\n");
      }
      System.out.print("info_end\n");
    }
    System.out.print("\n");
  }

  public static List getAdditionalBeans(String text, int timeout) throws Throwable {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final MyBeanDefinitionsRegistry registry = new MyBeanDefinitionsRegistry();

    final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);

    reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
    reader.setNamespaceAware(true);
    reader.setDocumentLoader(new DocumentLoader(){
      public Document loadDocument(final InputSource inputSource, final EntityResolver entityResolver, final ErrorHandler errorHandler,
                                   final int validationMode, final boolean namespaceAware) throws Exception {
        factory.setNamespaceAware(namespaceAware);
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        if (entityResolver != null) {
          builder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
          builder.setErrorHandler(errorHandler);
        }
        final Document document = builder.parse(inputSource);
        process(document.getDocumentElement(), EMPTY_INT_ARRAY);
        return document;
      }

      private void process(Element element, int[] path) {
        try {
          element.setUserData(COPY_KEY, path, null);
        }
        catch (Throwable e) {
          throw new RuntimeException(
            "class " + element.getClass().getName() + " doesn't conform to the interface " + Element.class.getName() + " specification:" +
            "\n     " + e + "\n" +
            "Check your classpath for outdated XML APIs (Xerces, etc.)");
        }
        int index = 0;
        final NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
          final Node node = nodes.item(i);
          if (node instanceof Element) {
            process((Element)node, append(path, index));
            index++;
          }
        }
      }
    });
    reader.setProblemReporter(new LenientProblemReporter());
    reader.setSourceExtractor(new SourceExtractor() {
      public Object extractSource(final Object sourceCandidate, final Resource definingResource) {
        return sourceCandidate instanceof Element ? ((Element)sourceCandidate).getUserData(COPY_KEY) : null;
      }
    });

    final ByteArrayResource resource = new ByteArrayResource(text.getBytes());
    final SemaphoreCopy reads = new SemaphoreCopy();
    reads.down();
    final Throwable[] exception = new Throwable[1];
    final Thread thread = new Thread() {
      public void run() {
        try {
          reader.loadBeanDefinitions(resource);
        }
        catch (Throwable e) {
          exception[0] = e;
        }
        finally {
          reads.up();
        }
      }
    };
    thread.start();

    if (!reads.waitFor(timeout)) {
      return null;
    }

    Throwable throwable = exception[0];
    if (throwable != null) {
      while (throwable instanceof BeanDefinitionStoreException) {
        final Throwable cause = ((BeanDefinitionStoreException)throwable).getRootCause();
        if (cause == null) break;
        throwable = cause;
      }
      throw throwable;
    }

    return registry.getResult();
  }

  public static int[] append(int[] array, int value) {
    array = realloc(array, array.length + 1);
    array[array.length - 1] = value;
    return array;
  }

  public static int[] realloc (final int [] array, final int newSize) {
    if (newSize == 0) {
      return EMPTY_INT_ARRAY;
    }

    final int oldSize = array.length;
    if (oldSize == newSize) {
      return array;
    }

    final int [] result = new int [newSize];
    System.arraycopy(array, 0, result, 0, Math.min (oldSize, newSize));
    return result;
  }

  private static class SemaphoreCopy {
    private int mySemaphore = 0;

    public synchronized void down() {
      mySemaphore++;
    }

    public synchronized void up() {
      mySemaphore--;
      if (mySemaphore == 0) {
        notifyAll();
      }
    }

    public synchronized boolean waitFor(final long timeout) {
      try {
        if (mySemaphore == 0) return true;
        final long startTime = System.currentTimeMillis();
        long waitTime = timeout;
        while (mySemaphore > 0) {
          wait(waitTime);
          final long elapsed = System.currentTimeMillis() - startTime;
          if (elapsed < timeout) {
            waitTime = timeout - elapsed;
          }
          else {
            break;
          }
        }
        return mySemaphore == 0;
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

  }


}
