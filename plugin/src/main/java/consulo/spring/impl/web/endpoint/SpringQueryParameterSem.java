package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.url.parameter.QueryParameterSem;
import consulo.endpoint.url.reference.UrlPathContext;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringQueryParameterSem implements QueryParameterSem {
    private final String myName;
    private final UrlPathContext myContext;

    private SpringQueryParameterSem(String name, UrlPathContext context) {
        myName = name;
        myContext = context;
    }

    @RequiredReadAction
    public static @Nullable SpringQueryParameterSem create(PsiParameter parameter) {
        PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.REQUEST_PARAM);
        if (annotation == null) {
            return null;
        }
        String name = SpringRequestMappingSearch.getParameterBindingName(parameter, annotation);
        PsiMethod method = PsiTreeUtil.getParentOfType(parameter, PsiMethod.class);
        if (name == null || method == null || SpringClientSearch.isClient(method.getContainingClass())) {
            return null;
        }
        List<SpringRequestMapping> mappings = SpringRequestMappingSearch.findMappings(method);
        if (mappings.isEmpty()) {
            return null;
        }
        return new SpringQueryParameterSem(name, new UrlPathContext(SpringMvcUrlTargetInfo.create(mappings.get(0))));
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public UrlPathContext getUrlPathContext() {
        return myContext;
    }
}
