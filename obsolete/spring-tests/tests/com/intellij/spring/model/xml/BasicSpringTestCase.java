/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 14, 2006
 * Time: 4:07:12 PM
 */
package com.intellij.spring.model.xml;

import com.intellij.openapi.application.ApplicationManager;
import consulo.ide.impl.idea.openapi.application.PathManager;
import consulo.util.lang.ref.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class BasicSpringTestCase extends UsefulTestCase {
  
  @Nullable
  protected static VirtualFile getFile(final String path) {

    final Ref<VirtualFile> result = new Ref<VirtualFile>(null);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        result.set(file);
      }
    });
    return result.get();
  }

   protected SpringBean findBean(final Beans beans, final String beanId) {
      SpringBean springBean = null;

      for (SpringBean bean : beans.getBeans()) {
        if (beanId.equals(bean.getBeanName())) {
          springBean = bean;
          break;
        }
      }
      assertNotNull(springBean);
      return springBean;
    }

  /**
   * Return relative path to the test data.
   *
   * @return relative path to the test data.
   */
  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/";
  }

  /**
   * Return absolute path to the test data. Not intended to be overrided.
   *
   * @return absolute path to the test data.
   */
  @NonNls
  protected final String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath();
  }
}