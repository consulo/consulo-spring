package consulo.spring.impl.graphql;

import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringGraphQLConstants {
    public static final String SPRING_MODULE_EXTENSION_ID = "spring";

    private static final String ANNOTATION_PACKAGE = "org.springframework.graphql.data.method.annotation.";

    public static final String QUERY_MAPPING = ANNOTATION_PACKAGE + "QueryMapping";
    public static final String MUTATION_MAPPING = ANNOTATION_PACKAGE + "MutationMapping";
    public static final String SUBSCRIPTION_MAPPING = ANNOTATION_PACKAGE + "SubscriptionMapping";
    public static final String SCHEMA_MAPPING = ANNOTATION_PACKAGE + "SchemaMapping";
    public static final String BATCH_MAPPING = ANNOTATION_PACKAGE + "BatchMapping";

    public static final String ARGUMENT = ANNOTATION_PACKAGE + "Argument";
    public static final String ARGUMENTS = ANNOTATION_PACKAGE + "Arguments";
    public static final String CONTEXT_VALUE = ANNOTATION_PACKAGE + "ContextValue";
    public static final String LOCAL_CONTEXT_VALUE = ANNOTATION_PACKAGE + "LocalContextValue";

    public static final List<String> HANDLER_ANNOTATIONS =
        List.of(QUERY_MAPPING, MUTATION_MAPPING, SUBSCRIPTION_MAPPING, SCHEMA_MAPPING, BATCH_MAPPING);

    public static final List<String> NON_SOURCE_PARAMETER_ANNOTATIONS =
        List.of(ARGUMENT, ARGUMENTS, CONTEXT_VALUE, LOCAL_CONTEXT_VALUE);

    public static final String QUERY_TYPE = "Query";
    public static final String MUTATION_TYPE = "Mutation";
    public static final String SUBSCRIPTION_TYPE = "Subscription";

    private SpringGraphQLConstants() {
    }
}
