package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.util.lang.PatternUtil;
import consulo.xml.dom.*;
import consulo.xml.dom.convert.DelimitedListProcessor;
import consulo.xml.language.psi.XmlAttributeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Dmitry Avdeev
 */
public class PackageListConverter extends Converter<Collection<PsiJavaPackage>> implements CustomReferenceConverter {
  @Override
  public Collection<PsiJavaPackage> fromString(@Nullable String s, ConvertContext context) {
    if (s == null) {
      return Collections.emptyList();
    }
    XmlAttributeValue xmlAttributeValue = ((GenericAttributeValue)context.getInvocationElement()).getXmlAttributeValue();

    if (xmlAttributeValue == null) {
      return Collections.emptyList();
    }
    PsiReference[] psiReferences = xmlAttributeValue.getReferences();
    Collection<PsiJavaPackage> list = new HashSet<>();
    for (PsiReference psiReference : psiReferences) {
      if (psiReference instanceof PsiPackageReference packageRef) {
        list.addAll(packageRef.getReferenceSet().resolvePackage());
      }
    }
    return list;
  }

  @Override
  public String toString(@Nullable Collection<PsiJavaPackage> psiPackages, ConvertContext context) {
    return null;
  }

  @Nonnull
  @Override
  public PsiReference[] createReferences(GenericDomValue genericDomValue, final PsiElement element, ConvertContext context) {
    final String text = genericDomValue.getStringValue();
    if (text == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    List<PsiReference> list = new ArrayList<>();
    new DelimitedListProcessor(",") {
      @Override
      protected void processToken(final int start, final int end, boolean delimitersOnly) {
        PackageReferenceSet referenceSet = new PackageReferenceSet(text.substring(start, end), element, 1 + start) {
          @Override
          @RequiredReadAction
          public Collection<PsiJavaPackage> resolvePackageName(PsiJavaPackage context, String packageName) {
            if (packageName.contains("*")) {
              Pattern pattern = PatternUtil.fromMask(packageName);
              PsiJavaPackage[] psiPackages = context.getSubPackages();
              List<PsiJavaPackage> packages = new ArrayList<>(psiPackages.length);
              for (PsiJavaPackage aPackage : psiPackages) {
                if (pattern.matcher(aPackage.getName()).matches()) {
                  packages.add(aPackage);
                }
              }
              return packages;
            } else {
              return super.resolvePackageName(context, packageName);
            }
          }
        };
        list.addAll(referenceSet.getReferences());
      }
    }.processText(text);
    return list.toArray(new PsiReference[list.size()]);
  }
}
