package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.Authority;
import consulo.endpoint.url.UrlPath;
import consulo.endpoint.url.UrlQueryParameter;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.endpoint.url.reference.DefaultFrameworkUrlPathSpecification;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public abstract class SpringUrlTargetInfoBase implements UrlTargetInfo {
    private final List<String> mySchemes;
    private final List<Authority> myAuthorities;
    private final String myPathText;
    private final UrlPath myPath;
    private final Set<String> myMethods;
    private final boolean myDeprecated;
    private final String mySource;
    private final SmartPsiElementPointer<PsiMethod> myPointer;
    private final @Nullable SmartPsiElementPointer<PsiAnnotation> myAnnotationPointer;
    private final List<SpringQueryParameter> myQueryParameters;
    private final Set<String> myContentTypes;

    @RequiredReadAction
    protected SpringUrlTargetInfoBase(
        List<String> schemes,
        List<Authority> authorities,
        String pathText,
        SpringRequestMapping mapping,
        List<SpringQueryParameter> queryParameters,
        Set<String> contentTypes
    ) {
        PsiMethod method = mapping.getMethod();
        PsiClass containingClass = method.getContainingClass();
        String httpMethod = mapping.getHttpMethod();
        mySchemes = schemes;
        myAuthorities = authorities;
        myPathText = pathText;
        myPath = DefaultFrameworkUrlPathSpecification.INSTANCE.parsePath(pathText);
        myMethods = httpMethod == null ? Set.of() : Set.of(httpMethod);
        myDeprecated = mapping.isDeprecated();
        mySource = (containingClass == null ? "" : String.valueOf(containingClass.getName())) + "." + method.getName();
        myPointer = SmartPointerManager.createPointer(method);
        PsiAnnotation annotation = mapping.getAnnotation();
        myAnnotationPointer = annotation == null ? null : SmartPointerManager.createPointer(annotation);
        myQueryParameters = queryParameters;
        myContentTypes = contentTypes;
    }

    @RequiredReadAction
    protected static Set<String> readContentTypes(SpringRequestMapping mapping, String... attributes) {
        Set<String> result = new LinkedHashSet<>();
        PsiAnnotation annotation = mapping.getAnnotation();
        if (annotation == null) {
            return result;
        }
        for (String attribute : attributes) {
            result.addAll(SpringRequestMappingSearch.readStrings(annotation, attribute));
        }
        return result;
    }

    public abstract boolean isClient();

    public String getPathText() {
        return myPathText;
    }

    public @Nullable PsiAnnotation getAnnotation() {
        return myAnnotationPointer == null ? null : myAnnotationPointer.getElement();
    }

    @Override
    public Iterable<UrlQueryParameter> getQueryParameters() {
        return List.copyOf(myQueryParameters);
    }

    @Override
    public Set<String> getContentTypes() {
        return myContentTypes;
    }

    @Override
    public List<String> getSchemes() {
        return mySchemes;
    }

    @Override
    public List<Authority> getAuthorities() {
        return myAuthorities;
    }

    @Override
    public UrlPath getPath() {
        return myPath;
    }

    @Override
    public Set<String> getMethods() {
        return myMethods;
    }

    @Override
    public boolean isDeprecated() {
        return myDeprecated;
    }

    @Override
    public String getSource() {
        return mySource;
    }

    @Override
    public @Nullable PsiElement resolveToPsiElement() {
        return myPointer.getElement();
    }
}
