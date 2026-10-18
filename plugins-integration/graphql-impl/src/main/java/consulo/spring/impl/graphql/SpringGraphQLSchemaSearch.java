package consulo.spring.impl.graphql;

import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringGraphQLSchemaSearch {
    private SpringGraphQLSchemaSearch() {
    }

    @RequiredReadAction
    public static List<GraphQLFieldDefinition> findFieldDefinitions(SpringGraphQLHandler handler) {
        return findFieldDefinitions(handler.getMethod().getProject(), handler.getTypeName(), handler.getFieldName());
    }

    @RequiredReadAction
    public static List<GraphQLFieldDefinition> findFieldDefinitions(Project project, String typeName, String fieldName) {
        List<GraphQLFieldDefinition> result = new ArrayList<>();
        GraphQLPsiSearchHelper.getInstance(project).processNamedElements(project, fieldName, GlobalSearchScope.projectScope(project), element -> {
            if (element instanceof GraphQLFieldDefinition field && typeName.equals(GraphQLPsiUtil.findContainingTypeName(field))) {
                result.add(field);
            }
            return true;
        });
        return result;
    }
}
