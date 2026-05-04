package com.intellij.spring.security.model.xml.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.model.xml.converters.roles.SpringSecurityRolesFactory;
import com.intellij.spring.security.references.SpringSecurityRolePsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Serega.Vasiliev
 */
public class SecurityExpressionRootMethodsConverter extends Converter<String> implements CustomReferenceConverter<String> {
  private static String HAS_ROLE_METHOD_NAME = "hasRole";

  public String toString(final String strings, final ConvertContext context) {
    return strings;
  }

  public String fromString(final String s, final ConvertContext context) {
    return s;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {

    PsiMethod method = getExpressionRootMethod(genericDomValue);
    Module module = context.getModule();
    if (method != null && module != null) {
      List<PsiReference> references = new ArrayList<PsiReference>();

      references.add(createReference(element, method, module));
      if (HAS_ROLE_METHOD_NAME.equals(method.getName())) {
        PsiReference psiReference = createRoleNameReference(element, module);

        if (psiReference != null) references.add(psiReference);
      }

      return references.toArray(new PsiReference[references.size()]);
    }

    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  private PsiReference createRoleNameReference(@NotNull PsiElement element, @NotNull Module module) {
    String roleName = getRoleName(element.getText());
    if (roleName != null) {
      return new SpringSecurityRolePsiReference(element, roleName, module);
    }
    return null;
  }

  @Nullable
  // get ROLE_SUPERVISOR from <intercept-url access="hasRole( 'ROLE_SUPERVISOR' )"/>
  private String getRoleName(@NotNull String text) {
    int i = text.indexOf("(");
    int s = text.indexOf("'");
    if (i > 0 && i < s) {
      String startString = text.substring(s+1);
      int e = startString.indexOf("'");
      if (e > 0) {
        return startString.substring(0, e).trim();
      }
    }

    return null;
  }

  public PsiMethod getExpressionRootMethod(GenericDomValue genericDomValue) {
    String stringValue = genericDomValue.getStringValue();
    Module module = genericDomValue.getModule();
    if (module != null && stringValue != null) {
      for (PsiMethod psiMethod : getExpressionRootMethods(module)) {
        if (stringValue.startsWith(psiMethod.getName())) {
          return psiMethod;
        }
      }
    }
    return null;
  }

  private static PsiClass getExpressionRootClass(@Nullable Module module) {
    if (module == null) return null;
    return JavaPsiFacade.getInstance(module.getProject()).findClass(SpringSecurityClassesConstants.SECURITY_EXPRESSION_ROOT,
                                                                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
  }

  protected PsiReferenceBase createReference(PsiElement psiElement, final PsiMethod psiMethod, final Module module) {
    String name = psiMethod.getName();
    TextRange textRange = TextRange.from(psiElement.getText().indexOf(name), name.length());
    return new PsiReferenceBase<PsiElement>(psiElement, textRange, true) {
      public PsiElement resolve() {
        return psiMethod;
      }

      public Object[] getVariants() {
        Set<Object> variants = new HashSet<Object>();

        variants.addAll(getExpressionRootMethods(module));
        variants.addAll(SpringSecurityRolesFactory.getInstance(module).getRoles());

        return variants.toArray(new Object[variants.size()]);
      }
    };
  }

  @NotNull
  public static List<PsiMethod> getExpressionRootMethods(@Nullable Module module) {
    return getExpressionRootMethods(getExpressionRootClass(module));
  }

  @NotNull
  public static List<PsiMethod> getExpressionRootMethods(@Nullable PsiClass psiClass) {
    List<PsiMethod> strings = new ArrayList<PsiMethod>();
    if (psiClass != null) {
      for (PsiMethod psiMethod : psiClass.getAllMethods()) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (!psiMethod.isConstructor() &&
            containingClass != null &&
            !CommonClassNames.JAVA_LANG_OBJECT.equals(containingClass.getQualifiedName()) &&
            PsiPrimitiveType.BOOLEAN.equals(psiMethod.getReturnType())) {
          strings.add(psiMethod);
        }
      }
    }

    return strings;
  }

}