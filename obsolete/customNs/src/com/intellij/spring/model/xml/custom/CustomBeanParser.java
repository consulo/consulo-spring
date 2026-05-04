/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.custom;

import java.io.*;
import java.util.logging.*;

/**
 * @author peter
 */
public class CustomBeanParser {

  private CustomBeanParser() {
  }

  public static void main(String[] args) {
    Logger.getLogger("").setLevel(Level.FINE);
    Logger.getLogger("").addHandler(new Handler() {
      public void publish(final LogRecord record) {
        final Throwable throwable = record.getThrown();
        if (throwable != null) {
          printException(throwable);
        }
      }

      public void flush() {
      }

      public void close() throws SecurityException {
      }
    });

    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    try {
      int timeout = Integer.parseInt(input.readLine());
      while ("input".equals(input.readLine())) {
        String tagText = decode(input.readLine());
        try {
          CustomBeanParserUtil.parseCustomBean(tagText, timeout);
        }
        finally {
          System.out.flush();
        }
      }
    }
    catch (Throwable e) {
      printException(e);
    }
    finally {
      try {
        input.close();
      }
      catch (IOException e) {
      }
      System.exit(0);
    }
  }

  static void printException(final Throwable e) {
    System.out.print("exception\n");
    final StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    System.out.print(encode(writer.toString()) + "\n\n");
    System.out.flush();
  }

  // cannot use StringUtil here since this class has to be runnable under jad1.3, while StringUtil evidently can't
  static String decode(final String s1) {
    StringBuilder buffer = new StringBuilder();
    int length = s1.length();
    boolean escaped = false;
    for (int idx = 0; idx < length; idx++) {
      char ch = s1.charAt(idx);
      if (!escaped) {
        if (ch == '\\') {
          escaped = true;
        }
        else {
          buffer.append(ch);
        }
      }
      else {
        switch (ch) {
          case'n':
            buffer.append('\n');
            break;

          case'r':
            buffer.append('\r');
            break;

          case'b':
            buffer.append('\b');
            break;

          case't':
            buffer.append('\t');
            break;

          case'f':
            buffer.append('\f');
            break;

          case'\'':
            buffer.append('\'');
            break;

          case'\"':
            buffer.append('\"');
            break;

          case'\\':
            buffer.append('\\');
            break;

          case'u':
            if (idx + 4 < length) {
              try {
                int code = Integer.valueOf(s1.substring(idx + 1, idx + 5), 16).intValue();
                idx += 4;
                buffer.append((char)code);
              }
              catch (NumberFormatException e) {
                buffer.append("\\u");
              }
            }
            else {
              buffer.append("\\u");
            }
            break;

          default:
            buffer.append(ch);
            break;
        }
        escaped = false;
      }
    }

    if (escaped) buffer.append('\\');
    return buffer.toString();
  }

  // cannot use StringUtil here since this class has to be runnable under jad1.3, while StringUtil evidently can't
  static String encode(final String s1) {
    StringBuilder buffer = new StringBuilder();
    for (int idx = 0; idx < s1.length(); idx++) {
      char ch = s1.charAt(idx);
      switch (ch) {
        case'\b':
          buffer.append("\\b");
          break;

        case'\t':
          buffer.append("\\t");
          break;

        case'\n':
          buffer.append("\\n");
          break;

        case'\f':
          buffer.append("\\f");
          break;

        case'\r':
          buffer.append("\\r");
          break;

        case'\"':
          buffer.append("\\\"");
          break;

        case'\\':
          buffer.append("\\\\");
          break;

        default:
          if (Character.isISOControl(ch)) {
            String hexCode = Integer.toHexString(ch).toUpperCase();
            buffer.append("\\u");
            int paddingCount = 4 - hexCode.length();
            while (paddingCount-- > 0) {
              buffer.append(0);
            }
            buffer.append(hexCode);
          }
          else {
            buffer.append(ch);
          }
      }
    }
    return buffer.toString();
  }
}
