/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.util.lang.Couple;
import consulo.util.lang.Pair;

import java.util.*;
import java.util.function.BiFunction;

/**
 * @author peter
 */
public class AopPsiTypePatternsUtil {
  private static final List<Pair<Couple<Class>, BiFunction>> ourAnders = new ArrayList<>();

  private static <T extends AopPsiTypePattern, V extends AopPsiTypePattern> void addAnder(Class<T> first, Class<V> second, BiFunction<T,V,AopPsiTypePattern> function) {
    ourAnders.add(Pair.create(Couple.of((Class) first, (Class) second), function));
  }

  static {
    addAnder(PsiPrimitiveTypePattern.class, PsiPrimitiveTypePattern.class,
        (psiPrimitiveTypePattern, psiPrimitiveTypePattern1) -> psiPrimitiveTypePattern.accepts(psiPrimitiveTypePattern1.getType())
               ? psiPrimitiveTypePattern : AopPsiTypePattern.FALSE
    );

    addAnder(AopPsiTypePattern.class, AndPsiTypePattern.class, (aopPsiTypePattern, aopPsiTypePattern1) -> {
      Set<AopPsiTypePattern> result = new HashSet<>();
      AopPsiTypePattern[] patterns = aopPsiTypePattern1.getPatterns();
      for (AopPsiTypePattern pattern : patterns) {
        AopPsiTypePattern pattern1 = conjunctPatterns(pattern, aopPsiTypePattern);
        if (pattern1 instanceof AndPsiTypePattern andTypePattern) {
          result.addAll(Arrays.asList(andTypePattern.getPatterns()));
        } else {
          result.add(pattern1);
        }
      }
      return new AndPsiTypePattern(result.toArray(new AopPsiTypePattern[result.size()]));
    });

    addAnder(AopPsiTypePattern.class, AopPsiTypePattern.class,
        (aopPsiTypePattern, aopPsiTypePattern1) -> new AndPsiTypePattern(aopPsiTypePattern, aopPsiTypePattern1)
    );
  }

  public static AopPsiTypePattern conjunctPatterns(AopPsiTypePattern pattern1, AopPsiTypePattern pattern2) {
    if (pattern1 == AopPsiTypePattern.FALSE || pattern2 == AopPsiTypePattern.FALSE) return AopPsiTypePattern.FALSE;
    if (pattern1 == AopPsiTypePattern.TRUE) return pattern2;
    if (pattern2 == AopPsiTypePattern.TRUE) return pattern1;

    for (Pair<Couple<Class>, BiFunction> ander : ourAnders) {
      Couple<Class> pair = ander.first;
      if (pair.first.isInstance(pattern1) && pair.second.isInstance(pattern2)) return (AopPsiTypePattern)ander.second.apply(pattern1, pattern2);
      if (pair.first.isInstance(pattern2) && pair.second.isInstance(pattern1)) return (AopPsiTypePattern)ander.second.apply(pattern2, pattern1);
    }
    throw new AssertionError();
  }

}
