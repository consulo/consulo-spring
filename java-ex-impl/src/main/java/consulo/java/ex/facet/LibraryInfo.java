/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.java.ex.facet;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author nik
 */
public class LibraryInfo {
  public static final LibraryInfo[] EMPTY_ARRAY = new LibraryInfo[0];

  private final @Nullable LibraryDownloadInfo myDownloadInfo;
  private final String myName;
  private @Nullable String myMd5;
  private final String[] myRequiredClasses;

  public LibraryInfo(
      String name,
      @Nullable String downloadingUrl,
      @Nullable String presentableUrl,
      @Nullable String md5,
      String... requiredClasses
  ) {
    myName = name;
    myMd5 = md5;
    myRequiredClasses = requiredClasses;
    if (downloadingUrl != null) {
      int dot = name.lastIndexOf('.');
      String prefix = name.substring(0, dot);
      String suffix = name.substring(dot);
      myDownloadInfo = new LibraryDownloadInfo(downloadingUrl, presentableUrl, prefix, suffix);
    }
    else {
      myDownloadInfo = null;
    }
  }

  public LibraryInfo(String name, @Nullable LibraryDownloadInfo downloadInfo, String... requiredClasses) {
    myName = name;
    myRequiredClasses = requiredClasses;
    myDownloadInfo = downloadInfo;
  }

  public String getName() {
    return myName;
  }

  public String[] getRequiredClasses() {
    return myRequiredClasses;
  }

  @Nullable
  public LibraryDownloadInfo getDownloadingInfo() {
    return myDownloadInfo;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LibraryInfo that = (LibraryInfo)o;

      return Objects.equals(myDownloadInfo, that.myDownloadInfo)
          && myName.equals(that.myName)
          && Arrays.equals(myRequiredClasses, that.myRequiredClasses);
  }

  @Override
  public int hashCode() {
    int result;
    result = (myDownloadInfo != null ? myDownloadInfo.hashCode() : 0);
    result = 31 * result + myName.hashCode();
    result = 31 * result + Arrays.hashCode(myRequiredClasses);
    return result;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Nullable
  public String getMd5() {
    return myMd5;
  }
}
