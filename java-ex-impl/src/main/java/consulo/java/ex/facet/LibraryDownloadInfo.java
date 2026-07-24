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

import consulo.util.lang.Comparing;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author nik
 */
public class LibraryDownloadInfo {
  @Nullable private final RemoteRepositoryInfo myRemoteRepository;
  private final String myRelativeDownloadUrl;
  private final String myFileNamePrefix;
  private final String myFileNameSuffix;
  @Nullable private final String myPresentableUrl;

  public LibraryDownloadInfo(@Nonnull RemoteRepositoryInfo remoteRepository,
                             @Nonnull @NonNls String relativeDownloadUrl,
                             @Nonnull @NonNls String fileNamePrefix,
                             @Nonnull @NonNls String fileNameSuffix) {
    myRemoteRepository = remoteRepository;
    myRelativeDownloadUrl = relativeDownloadUrl;
    myFileNamePrefix = fileNamePrefix;
    myFileNameSuffix = fileNameSuffix;
    myPresentableUrl = null;
  }

  public LibraryDownloadInfo(@Nonnull String downloadUrl, @Nullable String presentableUrl,
                             @Nonnull @NonNls String fileNamePrefix, @Nonnull @NonNls String fileNameSuffix) {
    myRemoteRepository = null;
    myRelativeDownloadUrl = downloadUrl;
    myFileNamePrefix = fileNamePrefix;
    myFileNameSuffix = fileNameSuffix;
    myPresentableUrl = presentableUrl != null ? presentableUrl : downloadUrl;
  }

  public LibraryDownloadInfo(@Nonnull String downloadUrl, @Nullable String presentableUrl,
                             @Nonnull @NonNls String fileNamePrefix) {
    this(downloadUrl, presentableUrl, fileNamePrefix, ".jar");
  }

  public LibraryDownloadInfo(@Nonnull String downloadUrl, @Nonnull @NonNls String fileNamePrefix) {
    this(downloadUrl, null, fileNamePrefix);
  }

  @Nonnull
  public String getDownloadUrl() {
    return myRemoteRepository != null ? getDownloadUrl(myRemoteRepository.getDefaultMirror()) : myRelativeDownloadUrl;
  }

  @Nonnull
  public String getDownloadUrl(String mirror) {
    return mirror + myRelativeDownloadUrl;
  }

  @Nullable
  public RemoteRepositoryInfo getRemoteRepository() {
    return myRemoteRepository;
  }

  @Nonnull
  public String getFileNamePrefix() {
    return myFileNamePrefix;
  }

  @Nonnull
  public String getFileNameSuffix() {
    return myFileNameSuffix;
  }

  @Nonnull
  public String getFileName() {
    return myFileNamePrefix + myFileNameSuffix;
  }

  @Nonnull
  public String getPresentableUrl() {
    return myPresentableUrl != null ? myPresentableUrl
        : myRemoteRepository != null ? myRemoteRepository.getDefaultMirror() : myRelativeDownloadUrl;
  }

  @Nonnull
  public String getPresentableUrl(String mirror) {
    return myPresentableUrl != null ? myPresentableUrl : mirror;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LibraryDownloadInfo that = (LibraryDownloadInfo)o;

    if (!myFileNamePrefix.equals(that.myFileNamePrefix)) return false;
    if (!myFileNameSuffix.equals(that.myFileNameSuffix)) return false;
    if (!Comparing.equal(myPresentableUrl, that.myPresentableUrl)) return false;
    if (!myRelativeDownloadUrl.equals(that.myRelativeDownloadUrl)) return false;
    if (!Comparing.equal(myRemoteRepository, that.myRemoteRepository)) return false;

    return true;
  }

  public int hashCode() {
    int result = myRemoteRepository != null ? myRemoteRepository.hashCode() : 0;
    result = 31 * result + (myRelativeDownloadUrl != null ? myRelativeDownloadUrl.hashCode() : 0);
    result = 31 * result + (myFileNamePrefix != null ? myFileNamePrefix.hashCode() : 0);
    result = 31 * result + (myFileNameSuffix != null ? myFileNameSuffix.hashCode() : 0);
    result = 31 * result + (myPresentableUrl != null ? myPresentableUrl.hashCode() : 0);
    return result;
  }
}
