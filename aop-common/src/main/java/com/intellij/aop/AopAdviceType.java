/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.jam.AopConstants;
import consulo.ui.image.Image;

/**
 * @author peter
 */
public enum AopAdviceType {
  BEFORE(AopConstants.BEFORE_ADVICE_ICON, true),
  AFTER(AopConstants.AFTER_ADVICE_ICON, false),
  AFTER_RETURNING(AopConstants.AFTER_RETURNING_ADVICE_ICON, false),
  AFTER_THROWING(AopConstants.AFTER_THROWING_ADVICE_ICON, false),
  AROUND(AopConstants.AROUND_ADVICE_ICON, true);

  private final Image myIcon;
  private final boolean myOnTheWayIn;

  private AopAdviceType(Image icon, boolean onTheWayIn) {
    myIcon = icon;
    myOnTheWayIn = onTheWayIn;
  }

  public Image getAdviceIcon() {
    return myIcon;
  }

  public boolean isOnTheWayIn() {
    return myOnTheWayIn;
  }
}
