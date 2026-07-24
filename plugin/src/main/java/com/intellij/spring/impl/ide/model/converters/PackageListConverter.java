package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.util.lang.PatternUtil;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.dom.*;
import consulo.xml.dom.convert.DelimitedListProcessor;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Dmitry Avdeev
 */
public class PackageListConverter extends Converter<Collection<PsiJavaPackage>> implements CustomReferenceConverter {

  public Collection<PsiJavaPackage> fromString(@Nullable @NonNls String s, ConvertContext context) {
    if (s == null) {
      return Collections.emptyList();
    }
    XmlAttributeValue xmlAttributeValue = ((GenericAttributeValue)context.getInvocationElement()).getXmlAttributeValue();

    if (xmlAttributeValue == null) {
      return Collections.emptyList();
    }
    PsiReference[] psiReferences = xmlAttributeValue.getReferences();
    Collection<PsiJavaPackage> list = new HashSet<PsiJavaPackage>();
    for (PsiReference psiReference : psiReferences) {
      if (psiReference instanceof PsiPackageReference) {
        list.addAll(((PsiPackageReference)psiReference).getReferenceSet().resolvePackage());
      }
    }
    return list;
  }

  public String toString(@Nullable Collection<PsiJavaPackage> psiPackages, ConvertContext context) {
    return null;
  }

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, final PsiElement element, ConvertContext context) {
    final String text = genericDomValue.getStringValue();
    if (text == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    final ArrayList<PsiReference> list = new ArrayList<PsiReference>();
    new DelimitedListProcessor(",") {
      protected void processToken(final int start, final int end, boolean delimitersOnly) {
        PackageReferenceSet referenceSet = new PackageReferenceSet(text.substring(start, end), element, 1 + start) {
          @Override
          public Collection<PsiJavaPackage> resolvePackageName(PsiJavaPackage context, String packageName) {
            if (packageName.contains("*")) {
              Pattern pattern = PatternUtil.fromMask(packageName);
              PsiJavaPackage[] psiPackages = context.getSubPackages();
              ArrayList<PsiJavaPackage> packages = new ArrayList<PsiJavaPackage>(psiPackages.length);
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
