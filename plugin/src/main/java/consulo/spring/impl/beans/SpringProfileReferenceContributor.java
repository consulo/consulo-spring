package consulo.spring.impl.beans;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.patterns.PsiJavaPatterns;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.psi.ResolveResult;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.module.Module;
import consulo.spring.impl.boot.properties.SpringConfigurationPropertySearch;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.impl.web.endpoint.SpringConfigValues;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static consulo.language.pattern.StandardPatterns.string;

/**
 * Profile names in {@code @Profile} resolve to the config files and properties which define or activate the profile:
 * {@code application-<profile>.*}, {@code spring.profiles.active} and {@code spring.config.activate.on-profile}.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
@ExtensionImpl
public final class SpringProfileReferenceContributor extends PsiReferenceContributor {
    private static final String PROFILE = "org.springframework.context.annotation.Profile";

    private static final String[] PROFILE_PROPERTIES = {
        "spring.profiles.active",
        "spring.profiles.include",
        "spring.profiles.default",
        "spring.config.activate.on-profile",
        "spring.profiles",
    };

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PsiJavaPatterns.literalExpression().insideAnnotationParam(string().oneOf(PROFILE), "value"),
            new PsiReferenceProvider() {
                @Override
                @RequiredReadAction
                public PsiReference[] getReferencesByElement(PsiElement element, ProcessingContext context) {
                    return createReferences(element);
                }
            }
        );
    }

    @Override
    public Language getLanguage() {
        return JavaLanguage.INSTANCE;
    }

    @RequiredReadAction
    private static PsiReference[] createReferences(PsiElement element) {
        if (!(element instanceof PsiLiteralExpression literal) || !(literal.getValue() instanceof String)) {
            return PsiReference.EMPTY_ARRAY;
        }

        String text = element.getText();
        if (text.length() < 2) {
            return PsiReference.EMPTY_ARRAY;
        }

        // the names of an expression like "dev & !cloud", each one its own reference; +1 for the opening quote
        List<PsiReference> references = new ArrayList<>();
        int start = -1;
        for (int i = 1; i <= text.length() - 1; i++) {
            boolean nameChar = i < text.length() - 1 && isNameChar(text.charAt(i));
            if (nameChar && start < 0) {
                start = i;
            }
            else if (!nameChar && start >= 0) {
                references.add(new ProfileReference(element, new TextRange(start, i), text.substring(start, i)));
                start = -1;
            }
        }
        if (references.isEmpty()) {
            // an empty literal still gets a reference, so completion works in @Profile("")
            references.add(new ProfileReference(element, new TextRange(1, text.length() - 1), ""));
        }
        return references.toArray(PsiReference.EMPTY_ARRAY);
    }

    private static boolean isNameChar(char c) {
        return !Character.isWhitespace(c) && c != '!' && c != '&' && c != '|' && c != '(' && c != ')' && c != '"' && c != ',';
    }

    private static final class ProfileReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
        private final String myProfile;

        private ProfileReference(PsiElement element, TextRange range, String profile) {
            super(element, range, true);
            myProfile = profile;
        }

        @Override
        @RequiredReadAction
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            Module module = ModuleUtilCore.findModuleForPsiElement(myElement);
            if (module == null || myProfile.isEmpty()) {
                return ResolveResult.EMPTY_ARRAY;
            }

            List<ResolveResult> results = new ArrayList<>();
            SpringConfigurationPropertySearch search = SpringConfigurationPropertySearch.getInstance(module.getProject());
            PsiManager psiManager = PsiManager.getInstance(module.getProject());
            for (VirtualFile file : search.findConfigFiles(module)) {
                if (myProfile.equals(SpringBeansCollector.getProfileOfConfigFile(file))) {
                    PsiFile psiFile = psiManager.findFile(file);
                    if (psiFile != null) {
                        results.add(new PsiElementResolveResult(psiFile));
                    }
                }
            }

            for (String property : PROFILE_PROPERTIES) {
                for (PsiElement definition : search.resolvePropertyKey(property, module)) {
                    String value = SpringConfigValues.getValue(module, definition);
                    if (value != null && mentions(value, myProfile)) {
                        results.add(new PsiElementResolveResult(definition));
                    }
                }
            }
            return results.toArray(ResolveResult.EMPTY_ARRAY);
        }

        private static boolean mentions(String value, String profile) {
            List<String> names = new ArrayList<>();
            SpringProfileExpression.collectNames(value, names);
            return names.contains(profile);
        }

        @Override
        @RequiredReadAction
        public @Nullable PsiElement resolve() {
            ResolveResult[] results = multiResolve(false);
            return results.length == 1 ? results[0].getElement() : null;
        }

        @Override
        @RequiredReadAction
        public Object[] getVariants() {
            Module module = ModuleUtilCore.findModuleForPsiElement(myElement);
            if (module == null) {
                return EMPTY_ARRAY;
            }

            Set<String> names = new LinkedHashSet<>();
            SpringModuleBeans beans = SpringBeansCollector.collect(module);
            if (beans != null) {
                for (SpringProfileInfo profile : beans.profiles()) {
                    names.add(profile.getName());
                }
            }
            else {
                for (VirtualFile file : SpringConfigurationPropertySearch.getInstance(module.getProject()).findConfigFiles(module)) {
                    String profile = SpringBeansCollector.getProfileOfConfigFile(file);
                    if (profile != null) {
                        names.add(profile);
                    }
                }
            }

            List<LookupElementBuilder> variants = new ArrayList<>();
            for (String name : names) {
                variants.add(LookupElementBuilder.create(name).withIcon(SpringImplIconGroup.springproperty()));
            }
            return variants.toArray();
        }
    }
}
