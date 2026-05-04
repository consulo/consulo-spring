package com.intellij.spring.security;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class SpringSecurityBundle {
  private static Reference<ResourceBundle> ourBundle;

  @NonNls private static final String PATH_TO_BUNDLE = "resources.messages.SpringSecurityBundle";

  private SpringSecurityBundle() {
  }

  public static String message(@PropertyKey(resourceBundle = "resources.messages.SpringSecurityBundle")String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (ourBundle != null) bundle = ourBundle.get();
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}

