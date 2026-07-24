package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;

import consulo.java.ex.facet.LibraryInfo;
import consulo.module.Module;

public class LibrariesInfo {
    private LibraryInfo[] myLibs;
  private Module myModule;
  private String myName;

  public LibrariesInfo(LibraryInfo[] libs, Module module, String name) {
    myLibs = libs;
    myModule = module;
    myName = name;
  }

  public LibraryInfo[] getLibs() {
    return myLibs;
  }

  public void setLibs(LibraryInfo[] libs) {
    myLibs = libs;
  }

  public consulo.module.Module getModule() {
    return myModule;
  }

  public void setModule(consulo.module.Module module) {
    myModule = module;
  }

  public String getName() {
    return myName;
  }

  public void setName(String name) {
    myName = name;
  }
}
