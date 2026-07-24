/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

/**
 * @author peter
 */
public enum PointcutMatchDegree {
  FALSE,MAYBE,TRUE;

  public static PointcutMatchDegree and(PointcutMatchDegree d1, PointcutMatchDegree d2) {
    return PointcutMatchDegree.class.getEnumConstants()[Math.min(d1.ordinal(), d2.ordinal())];
  }

  public static PointcutMatchDegree or(PointcutMatchDegree d1, PointcutMatchDegree d2) {
    return PointcutMatchDegree.class.getEnumConstants()[Math.max(d1.ordinal(), d2.ordinal())];
  }

  public static PointcutMatchDegree not(PointcutMatchDegree d1) {
    PointcutMatchDegree[] constants = PointcutMatchDegree.class.getEnumConstants();
    return constants[constants.length - 1 - d1.ordinal()];
  }

  public static PointcutMatchDegree valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }

  public boolean isTrue() {
    return this == TRUE;
  }
}
