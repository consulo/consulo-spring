package consulo.spring.impl.graphql;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.PsiIdentifier;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.lang.jsgraphql.icon.GraphQLIconGroup;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import consulo.spring.graphql.localize.SpringGraphQLLocalize;

import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ExtensionImpl
public final class SpringGraphQLHandlerLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    public Language getLanguage() {
        return JavaLanguage.INSTANCE;
    }

    @Override
    @RequiredReadAction
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (!(element instanceof PsiIdentifier) || !(element.getParent() instanceof PsiMethod method) || method.getNameIdentifier() != element) {
            return;
        }

        SpringGraphQLHandler handler = SpringGraphQLHandlerSearch.getHandler(method);
        if (handler == null) {
            return;
        }

        List<GraphQLFieldDefinition> fields = SpringGraphQLSchemaSearch.findFieldDefinitions(handler);
        if (fields.isEmpty()) {
            return;
        }

        result.add(NavigationGutterIconBuilder.create(GraphQLIconGroup.gutterGraphql())
            .setTargets(fields)
            .setPopupTitle(SpringGraphQLLocalize.gutterNavigateToSchemaTitle())
            .setTooltipText(SpringGraphQLLocalize.gutterNavigateToSchemaTooltip())
            .createLineMarkerInfo(element));
    }
}
