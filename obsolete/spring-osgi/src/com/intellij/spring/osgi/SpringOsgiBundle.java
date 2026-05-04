package com.intellij.spring.osgi;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class SpringOsgiBundle {
  private static Reference<ResourceBundle> ourBundle;

  @NonNls private static final String PATH_TO_BUNDLE = "resources.messages.SpringOsgiBundle";

  private SpringOsgiBundle() {
  }

  public static String message(@PropertyKey(resourceBundle = "resources.messages.SpringOsgiBundle")String key, Object... params) {
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

