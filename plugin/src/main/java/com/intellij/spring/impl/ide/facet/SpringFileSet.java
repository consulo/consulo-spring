package com.intellij.spring.impl.ide.facet;

import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.ui.ex.awt.ElementsChooser;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.virtualFileSystem.pointer.VirtualFilePointerManager;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringFileSet implements ElementsChooser.ElementProperties, Disposable {
  private static final String ID_PREFIX = "fileset";
  private boolean myAutodetected;

  public static String getUniqueId(Set<SpringFileSet> list) {
    int index = 0;
    for (SpringFileSet fileSet : list) {
      if (fileSet.getId().startsWith(ID_PREFIX)) {
        String s = fileSet.getId().substring(ID_PREFIX.length());
        try {
          int i = Integer.parseInt(s);
          index = Math.max(i, index);
        }
        catch (NumberFormatException ignored) {

        }
      }
    }
    return ID_PREFIX + (index + 1);
  }

  public static String getUniqueName(String prefix, Set<SpringFileSet> list) {
    int index = 0;
    for (SpringFileSet fileSet : list) {
      if (fileSet.getName().startsWith(prefix)) {
        String s = fileSet.getName().substring(prefix.length());
        int i;
        try {
          i = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
          i = 0;
        }
        index = Math.max(i + 1, index);
      }
    }
    return index == 0 ? prefix : prefix + index;
  }

  @Nonnull
  private final String myId;
  private String myName;
  private final Map<String, VirtualFilePointer> myFiles = new TreeMap<>();
  private final List<String> myDependencies = new ArrayList<>();
  private boolean myRemoved;

  public SpringFileSet(@Nonnull String id, @Nonnull String name, @Nonnull Disposable parent) {
    myId = id;
    myName = name;
    Disposer.register(parent, this);
  }

  public SpringFileSet(SpringFileSet original) {
    myId = original.myId;
    myName = original.myName;
    myFiles.putAll(original.myFiles);
    myDependencies.addAll(original.myDependencies);
    myAutodetected = original.isAutodetected();
    myRemoved = original.isRemoved();
  }

  public boolean isNew() {
    return false;
  }

  public boolean isAutodetected() {
    return myAutodetected;
  }

  public void setAutodetected(boolean autodetected) {
    myAutodetected = autodetected;
  }

  public boolean isRemoved() {
    return myRemoved;
  }

  @Nonnull
  public String getId() {
    return myId;
  }

  public String getName() {
    return myName;
  }

  public void setName(@Nonnull String name) {
    myName = name;
  }

  @Nonnull
  public Collection<VirtualFilePointer> getFiles() {
    return myFiles.values();
  }

  public List<String> getDependencies() {
    return myDependencies;
  }

  public void removeDependency(String dependency) {
    myDependencies.remove(dependency);
  }

  public void setDependencies(List<String> dependencies) {
    myDependencies.clear();
    myDependencies.addAll(dependencies);
  }

  public void addDependency(String dep) {
    myDependencies.add(dep);
  }

  public void addFile(@NonNls String url) {
    if (!StringUtil.isEmptyOrSpaces(url)) {
      VirtualFilePointer filePointer = VirtualFilePointerManager.getInstance().create(url, this, null);
      myFiles.put(filePointer.getUrl(), filePointer);
    }
  }

  public void addFile(@Nonnull VirtualFile file) {
    addFile(file.getUrl());
  }

  public void removeFile(VirtualFilePointer file) {
    myFiles.remove(file.getUrl());
  }

  public boolean hasFile(@Nullable VirtualFile file) {
    if (file == null) {
      return false;
    }
    for (VirtualFilePointer pointer : myFiles.values()) {
      VirtualFile virtualFile = pointer.getFile();
      if (virtualFile != null && file.equals(virtualFile)) {
        return true;
      }
    }
    return false;
  }

  @Nonnull
  public SpringFileSet cloneTo(Disposable parent) {
    SpringFileSet fileSet = SpringFileSetFactory.create(getType(), myId, myName, parent);
    fileSet.myDependencies.addAll(myDependencies);
    fileSet.myAutodetected = isAutodetected();
    fileSet.myRemoved = isRemoved();
    for (VirtualFilePointer pointer : myFiles.values()) {
      fileSet.addFile(pointer.getUrl());
    }
    return fileSet;
  }

  @Nonnull
  public abstract String getType();

  @Override
  public Color getColor() {
    return null;
  }

  public String toString() {
    return myName;
  }

  public void setRemoved(boolean removed) {
    myRemoved = removed;
  }

  @Override
  public void dispose() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpringFileSet)) return false;
    SpringFileSet that = (SpringFileSet)o;
    return myAutodetected == that.myAutodetected &&
      myRemoved == that.myRemoved &&
      Objects.equals(myId, that.myId) &&
      Objects.equals(myName, that.myName) &&
      Objects.equals(myFiles, that.myFiles) &&
      Objects.equals(myDependencies, that.myDependencies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myAutodetected, myId, myName, myFiles, myDependencies, myRemoved);
  }
}
