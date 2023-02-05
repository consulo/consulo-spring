/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopModuleService;
import com.intellij.aop.jam.AopPointcutImpl;
import com.intellij.java.impl.codeInsight.completion.util.MethodParenthesesHandler;
import com.intellij.java.impl.psi.AbstractQualifiedReference;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.MethodSignature;
import consulo.language.ast.ASTNode;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.TailType;
import consulo.language.editor.completion.lookup.TailTypeDecorator;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.impl.psi.CheckUtil;
import consulo.language.psi.*;
import consulo.language.psi.resolve.BaseScopeProcessor;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.xml.XmlElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author peter
 */
public class AopReferenceExpression extends AbstractQualifiedReference<AopReferenceExpression> implements AopReferenceQualifier {

  public AopReferenceExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopReferenceExpression";
  }

  enum Resolvability {
    PLAIN,
    POLYVARIANT,
    NONE
  }

  @Nullable
  public AopReferenceQualifier getGeneralizedQualifier() {
    return findChildByClass(AopReferenceQualifier.class);
  }


  @Nonnull
  public Resolvability getResolvability() {
    if (isDoubleDot()) return Resolvability.NONE;

    final AopReferenceQualifier qualifier = getGeneralizedQualifier();
    if (qualifier != null && qualifier.getResolvability() != Resolvability.PLAIN) return Resolvability.NONE;

    return findChildByType(AopElementTypes.AOP_ASTERISK) != null ? (qualifier == null ? Resolvability.NONE : Resolvability.POLYVARIANT) : Resolvability.PLAIN;
  }

  public final boolean isDoubleDot() {
    return findChildByType(AopElementTypes.AOP_DOT_DOT) != null;
  }

  public AopPointcutExpressionFile getContainingFile() {
    return (AopPointcutExpressionFile)super.getContainingFile();
  }

  private boolean isAcceptableTarget(PsiElement element) {
    if (element instanceof PsiParameter) return true;
    final AopMemberReferenceExpression methodRef = PsiTreeUtil.getParentOfType(this, AopMemberReferenceExpression.class);
    if (methodRef == null && !isPointcutReference() && element instanceof PsiMethod) return false;
    return element instanceof PsiNamedElement && !(element instanceof PsiField);
  }

  @Nonnull
  public AbstractQualifiedReference shortenReferences() {
    return this;
  }

  public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
    CheckUtil.checkWritable(this);
    return element instanceof PsiClass ? replaceReference(((PsiClass)element).getQualifiedName()) : super.bindToElement(element);
  }

  protected ResolveResult[] resolveInner() {
    final Pattern regex = getRegex();
    final AbstractQualifiedReferenceResolvingProcessor processor = new AbstractQualifiedReferenceResolvingProcessor() {
      protected final void process(PsiElement element) {
        if (isAcceptableTarget(element)) {
          final String name = ((PsiNamedElement)element).getName();
          if (name != null && regex.matcher(name).matches() && isAccessible(element)) {
            if (element instanceof PsiMethod) {
              if (getParent() instanceof AopReferenceQualifier) return;

              final AopPointcutImpl pointcut = AopModuleService.getPointcut((PsiMethod)element);
              if (pointcut != null) {
                addResult(new AopPointcutResolveResult(pointcut));
                return;
              }
            }
            addResult(new PsiElementResolveResult(element));
          }
        }
      }
    };
    processVariantsInner(processor);
    final Set<ResolveResult> results = processor.getResults();
    return results.toArray(new ResolveResult[results.size()]);
  }

  @Nullable
  public AopPointcut resolvePointcut() {
    final ResolveResult[] results = multiResolve(false);
    return results.length == 1 && results[0] instanceof AopPointcutResolveResult ? ((AopPointcutResolveResult)results[0]).getPointcut() : null;
  }

  protected boolean processVariantsInner(PsiScopeProcessor processor) {
    return getResolvability() == Resolvability.NONE || super.processVariantsInner(processor);
  }

  protected boolean processUnqualifiedVariants(final PsiScopeProcessor processor) {
    final ResolveState state = ResolveState.initial();
    if (!getContainingFile().processDeclarations(processor, state, null, this)) return false;

    for (final PsiParameter parameter : getContainingFile().getAopModel().resolveParameters(getOwnText())) {
      if (!processor.execute(parameter, state)) return false;
    }

    PsiClass psiClass = PsiTreeUtil.getContextOfType(this, PsiClass.class, true);
    JavaPsiFacade facade = JavaPsiFacade.getInstance(getProject());
    while (psiClass != null) {
      if (!psiClass.processDeclarations(processor, state, null, this)) return false;
      PsiClass parentClass = PsiTreeUtil.getContextOfType(psiClass, PsiClass.class, true);
      if (parentClass == null) {
        final String fqName = psiClass.getQualifiedName();
        if (fqName != null) {
          final PsiJavaPackage psiPackage = facade.findPackage(StringUtil.getPackageName(fqName));
          if (psiPackage != null && !psiPackage.processDeclarations(processor, state, null, this)) return false;
        }
      }
      psiClass = parentClass;
    }

    PsiJavaPackage psiPackage = facade.findPackage("java.lang");
    if (psiPackage != null && !psiPackage.processDeclarations(processor, state, null, this)) return false;

    psiPackage = facade.findPackage("");
    if (psiPackage != null && !psiPackage.processDeclarations(processor, state, null, this)) return false;

    return true;
  }

  protected PsiElement getReferenceNameElement() {
    return findChildByType(AopElementTypes.AOP_IDENTIFIER);
  }

  @Nonnull
  protected final AopReferenceExpression parseReference(final String newText) {
    final AopPointcutExpressionFile file = (AopPointcutExpressionFile)PsiFileFactory.getInstance(getProject())
                                                                                    .createFileFromText("a",
                                                                                                        AopPointcutExpressionFileType.INSTANCE,
                                                                                                        newText + "()");
    final PsiPointcutReferenceExpression pointcutExpression = (PsiPointcutReferenceExpression)file.getPointcutExpression();
    return ObjectUtil.assertNotNull(ObjectUtil.assertNotNull(pointcutExpression).getReferenceExpression());
  }

  @Nullable
  protected PsiElement getSeparator() {
    return findChildByType(AopElementTypes.AOP_DOTS);
  }

  protected boolean isAccessible(final PsiElement element) {
    if (element instanceof PsiMethod) {
      if (!((PsiMethod)element).hasModifierProperty(PsiModifier.PUBLIC) && getContainingFile().getContext() instanceof XmlElement)
        return false;
    }
    return super.isAccessible(element);
  }

  public LookupElement[] getVariants() {
    final Set<MethodSignature> signatures = new HashSet<>();
    final List<LookupElement> list = new ArrayList<LookupElement>();
    if (isPointcutReference()) {
      final LocalAopModel model = getContainingFile().getAopModel();
      final PsiMethod pointcutMethod = model.getPointcutMethod();

      final Set<String> qnames = new HashSet<String>();
      final String prefix = getText().substring(0, getRangeInElement().getStartOffset());
      processVariantsInner(new BaseScopeProcessor() {
        public boolean execute(final PsiElement element, final ResolveState state) {
          if (element instanceof PsiMethod && element != pointcutMethod && PsiUtilBase.getOriginalElement(element,
                                                                                                          PsiMethod.class) != pointcutMethod && isAccessible(
            element)) {
            final PsiMethod method = (PsiMethod)element;
            if (method.getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) != null) {
              final String methodName = method.getName();
              list.add(LookupElementBuilder.create(prefix + methodName)
                                           .withIcon(AopConstants.POINTCUT_ICON)
                                           .withInsertHandler(new MethodParenthesesHandler(method, true)));
              final PsiClass aClass = method.getContainingClass();
              if (aClass != null && !(aClass instanceof PsiAnonymousClass)) {
                PsiFile file = aClass.getContainingFile().getOriginalFile();
                if (file instanceof PsiJavaFile) {
                  final PsiJavaFile javaFile = (PsiJavaFile)file;
                  final String packageName = javaFile.getPackageName();
                  final String prefix = StringUtil.isEmpty(packageName) ? "" : packageName + ".";
                  qnames.add(prefix + aClass.getName() + "." + methodName);
                }
              }
            }
          }
          return true;
        }
      });
      for (final AopPointcut pointcut : model.getPointcuts()) {
        final PsiElement element = pointcut.getIdentifyingPsiElement();
        if ((element instanceof PsiAnnotation)) {
          final PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
          assert method != null;
          if (method != pointcutMethod && PsiUtilBase.getOriginalElement(method,
                                                                         PsiMethod.class) != pointcutMethod && isAccessible(method)) {
            final String qname = pointcut.getQualifiedName().getStringValue();
            if (qname != null && qname.startsWith(prefix) && !qnames.contains(qname)) {
              list.add(LookupElementBuilder.create(qname).
                withIcon(AopConstants.POINTCUT_ICON).
                                             withInsertHandler(new MethodParenthesesHandler(method, false)));
            }
          }
        }
      }
    }
    else {
      processVariantsInner(new BaseScopeProcessor() {
        public boolean execute(final PsiElement element, final ResolveState state) {
          if (isAcceptableTarget(element)) {
            final PsiNamedElement namedElement = (PsiNamedElement)element;
            final String name = namedElement.getName();
            assert name != null;
            LookupElementBuilder item = LookupElementBuilder.create(namedElement, name);
            if (element instanceof PsiMethod) {
              if (!signatures.add(((PsiMethod)element).getSignature(state.get(PsiSubstitutor.KEY)))) {
                return true;
              }

              item = item.setInsertHandler(new MethodParenthesesHandler((PsiMethod)element, true));
            }
            if (element instanceof PsiPackage) {
              list.add(TailTypeDecorator.withTail(item, TailType.DOT));
            }
            else {
              list.add(item);
            }
          }
          return true;
        }
      });
    }

    return list.toArray(new LookupElement[list.size()]);
  }

  public final boolean isPointcutReference() {
    return getParent() instanceof PsiPointcutReferenceExpression;
  }

  public final boolean isAnnotationReference() {
    final PsiElement parent = getParent();
    if (!(parent instanceof AopReferenceHolder)) return false;

    final PsiElement grandParent = parent.getParent();
    return grandParent instanceof AopParameterList
      ? grandParent.getParent() instanceof PsiAtPointcutDesignator
      : grandParent instanceof PsiAtPointcutDesignator;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    final String text = getText().trim();
    if ("*".equals(text)) return Arrays.asList(AopPsiTypePattern.TRUE);

    final AopReferenceQualifier qualifier = getGeneralizedQualifier();
    if (qualifier != null) {
      final Collection<AopPsiTypePattern> patterns = qualifier.getPatterns();
      if (patterns.isEmpty()) return patterns;

      final boolean doubleDot = isDoubleDot();
      final String ownText = getOwnText();
      if (patterns.size() == 1) {
        final AopPsiTypePattern pattern = patterns.iterator().next();
        final String prefix;
        if (pattern instanceof PsiClassTypePattern) {
          prefix = ((PsiClassTypePattern)pattern).getText();
        }
        else if (pattern == AopPsiTypePattern.TRUE) {
          prefix = "*";
        }
        else {
          prefix = null;
        }
        if (prefix != null) {
          return Arrays.asList((AopPsiTypePattern)new PsiClassTypePattern(prefix + (doubleDot ? ".." : ".") + ownText));
        }
      }


      final AopPsiTypePattern rightPattern = "*".equals(ownText) ? PsiClassTypePattern.TRUE : new PsiClassTypePattern(ownText);
      return ContainerUtil.map2List(patterns,
                                    (Function<AopPsiTypePattern, AopPsiTypePattern>)aopPsiTypePattern -> new ConcatenationPattern(
                                      aopPsiTypePattern,
                                      rightPattern,
                                      doubleDot));
    }
    else {
      final PsiElement psiElement = resolve();
      if (psiElement instanceof PsiClass) {
        final String qualifiedName = ((PsiClass)psiElement).getQualifiedName();
        if (qualifiedName != null) {
          return Arrays.asList((AopPsiTypePattern)new PsiClassTypePattern(qualifiedName));
        }
      }
    }
    return Arrays.asList((AopPsiTypePattern)new PsiClassTypePattern(text));
  }

  public String getTypePattern() {
    if (getGeneralizedQualifier() == null) {
      final PsiElement psiElement = resolve();
      if (psiElement instanceof PsiClass) {
        final String qualifiedName = ((PsiClass)psiElement).getQualifiedName();
        if (qualifiedName != null) {
          return "'_:[regex(" + qualifiedName.replaceAll("\\.", "\\\\.") + ")]";
        }
      }
    }

    final String text = getText().replaceAll(" ", "");
    String regex = "*".equals(text) ? ".*" : text.
      //replaceAll("([\\[\\]\\^\\(\\)\\{\\}\\-])", "\\\\$1").
        replaceAll("\\*", "\\[\\^\\\\.]\\+").
        replaceAll("\\.", "\\\\.").
        replaceAll("\\\\.\\\\.", "\\\\..*\\\\.");
    return "'_:[regex(" + regex + ")]";
  }

  public Pattern getRegex() {
    return Pattern.compile(getOwnText().replaceAll("\\*", ".*"));
  }

  private String getOwnText() {
    return getRangeInElement().substring(getText());
  }

  private static class AopPointcutResolveResult extends PsiElementResolveResult {
    @Nonnull
    private final AopPointcut myPointcut;

    public AopPointcutResolveResult(@Nonnull final AopPointcutImpl pointcut) {
      super(ObjectUtil.assertNotNull(pointcut.getPsiElement()));
      myPointcut = pointcut;
    }

    @Nonnull
    public AopPointcut getPointcut() {
      return myPointcut;
    }
  }
}
