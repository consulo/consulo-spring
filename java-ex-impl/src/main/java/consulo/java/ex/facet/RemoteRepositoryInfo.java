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

import consulo.logging.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

/**
 * @author nik
 */
public class RemoteRepositoryInfo {
  private static final Logger LOG = Logger.getInstance(RemoteRepositoryInfo.class);
  private final String myId;
  private final String myPresentableName;
  private final String[] myMirrors;

  public RemoteRepositoryInfo(@Nonnull @NonNls String id, @Nonnull @Nls String presentableName, @Nonnull @NonNls String[] mirrors) {
    myId = id;
    LOG.assertTrue(mirrors.length > 0);
    myPresentableName = presentableName;
    myMirrors = mirrors;
  }

  public String getId() {
    return myId;
  }

  public String getPresentableName() {
    return myPresentableName;
  }

  public String[] getMirrors() {
    return myMirrors;
  }

  public String getDefaultMirror() {
    return myMirrors[0];
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RemoteRepositoryInfo that = (RemoteRepositoryInfo)o;
    return myId.equals(that.myId);

  }

  public int hashCode() {
    return myId.hashCode();
  }
}
