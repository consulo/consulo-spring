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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.Objects;

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

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LibraryDownloadInfo that = (LibraryDownloadInfo)o;

    return myFileNamePrefix.equals(that.myFileNamePrefix)
      && myFileNameSuffix.equals(that.myFileNameSuffix)
      && Objects.equals(myPresentableUrl, that.myPresentableUrl)
      && myRelativeDownloadUrl.equals(that.myRelativeDownloadUrl)
      && Objects.equals(myRemoteRepository, that.myRemoteRepository);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(myRemoteRepository);
    result = 31 * result + Objects.hashCode(myRelativeDownloadUrl);
    result = 31 * result + Objects.hashCode(myFileNamePrefix);
    result = 31 * result + Objects.hashCode(myFileNameSuffix);
    result = 31 * result + Objects.hashCode(myPresentableUrl);
    return result;
  }
}
