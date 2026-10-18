package consulo.spring.impl.graphql;

import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.spring.graphql.localize.SpringGraphQLLocalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringGraphQLSchemaLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    public Language getLanguage() {
        return GraphQLLanguage.INSTANCE;
    }

    @Override
    @RequiredReadAction
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (element.getFirstChild() != null
            || !(element.getParent() instanceof GraphQLIdentifier identifier)
            || !(identifier.getParent() instanceof GraphQLFieldDefinition field)) {
            return;
        }

        String typeName = GraphQLPsiUtil.findContainingTypeName(field);
        String fieldName = field.getName();
        if (typeName == null || fieldName == null) {
            return;
        }

        List<PsiMethod> methods = new ArrayList<>();
        for (SpringGraphQLHandler handler : SpringGraphQLHandlerSearch.findHandlers(element.getProject(), typeName, fieldName)) {
            methods.add(handler.getMethod());
        }
        if (methods.isEmpty()) {
            return;
        }

        result.add(NavigationGutterIconBuilder.create(PlatformIconGroup.gutterImplementedmethod())
            .setTargets(methods)
            .setPopupTitle(SpringGraphQLLocalize.gutterNavigateToHandlerTitle())
            .setTooltipText(SpringGraphQLLocalize.gutterNavigateToHandlerTooltip())
            .createLineMarkerInfo(element));
    }
}
