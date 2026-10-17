package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiArrayType;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiEnumConstant;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiPrimitiveType;
import com.intellij.java.language.psi.PsiRecordComponent;
import com.intellij.java.language.psi.PsiSubstitutor;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiTypeParameter;
import com.intellij.java.language.psi.PsiWildcardType;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.oas.OasSchema;
import consulo.endpoint.oas.OasSchemaFormat;
import consulo.endpoint.oas.OasSchemaType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
public final class SpringOasSchemaFactory {
    private static final int MAX_DEPTH = 3;

    private static final Set<String> WRAPPERS = Set.of(
        "java.util.Optional",
        "org.springframework.http.ResponseEntity",
        "org.springframework.http.HttpEntity",
        "reactor.core.publisher.Mono",
        "java.util.concurrent.CompletableFuture",
        "java.util.concurrent.CompletionStage",
        "java.util.concurrent.Callable",
        "org.springframework.web.context.request.async.DeferredResult",
        "org.springframework.web.context.request.async.WebAsyncTask"
    );

    private static final Set<String> ARRAY_WRAPPERS = Set.of("reactor.core.publisher.Flux", "java.util.stream.Stream");

    private static final Set<String> BINARY_TYPES = Set.of(
        "org.springframework.web.multipart.MultipartFile",
        "jakarta.servlet.http.Part",
        "org.springframework.core.io.Resource",
        "java.io.InputStream",
        "java.io.File"
    );

    private SpringOasSchemaFactory() {
    }

    @RequiredReadAction
    public static @Nullable OasSchema create(PsiType type) {
        return create(type, 0, new HashSet<>());
    }

    @RequiredReadAction
    public static PsiType unwrap(PsiType type) {
        PsiType current = type;
        for (int i = 0; i < MAX_DEPTH && current instanceof PsiClassType classType; i++) {
            PsiClass psiClass = classType.resolve();
            if (psiClass == null || !WRAPPERS.contains(psiClass.getQualifiedName())) {
                break;
            }
            PsiType[] parameters = classType.getParameters();
            if (parameters.length == 0) {
                break;
            }
            current = parameters[0];
        }
        return current;
    }

    @RequiredReadAction
    public static boolean isBinary(PsiType type) {
        PsiType element = type instanceof PsiArrayType array ? array.getComponentType() : type;
        if (!(element instanceof PsiClassType classType)) {
            return false;
        }
        PsiClass psiClass = classType.resolve();
        return psiClass != null && BINARY_TYPES.contains(psiClass.getQualifiedName());
    }

    @RequiredReadAction
    public static boolean isVoid(@Nullable PsiType type) {
        if (type == null || PsiType.VOID.equals(type)) {
            return true;
        }
        return type instanceof PsiClassType classType && "java.lang.Void".equals(classType.rawType().getCanonicalText());
    }

    @RequiredReadAction
    private static @Nullable OasSchema create(PsiType type, int depth, Set<String> visiting) {
        if (isVoid(type)) {
            return null;
        }
        if (type instanceof PsiPrimitiveType primitive) {
            return simple(primitive.getCanonicalText());
        }
        if (type instanceof PsiArrayType array) {
            PsiType component = array.getComponentType();
            if (PsiType.BYTE.equals(component)) {
                return schema(OasSchemaType.STRING, OasSchemaFormat.BYTE);
            }
            return arrayOf(create(component, depth, visiting));
        }
        if (type instanceof PsiWildcardType wildcard) {
            PsiType bound = wildcard.getBound();
            return bound == null ? object() : create(bound, depth, visiting);
        }
        if (!(type instanceof PsiClassType classType)) {
            return object();
        }

        PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
        PsiClass psiClass = resolveResult.getElement();
        if (psiClass == null) {
            OasSchema byName = simple(classType.rawType().getCanonicalText());
            return byName != null ? byName : object();
        }
        if (psiClass instanceof PsiTypeParameter) {
            return object();
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null) {
            return object();
        }
        OasSchema simple = simple(qualifiedName);
        if (simple != null) {
            return simple;
        }
        if (BINARY_TYPES.contains(qualifiedName)) {
            return schema(OasSchemaType.STRING, OasSchemaFormat.BINARY);
        }
        if (WRAPPERS.contains(qualifiedName)) {
            PsiType[] parameters = classType.getParameters();
            return parameters.length == 0 ? object() : create(parameters[0], depth, visiting);
        }
        if (ARRAY_WRAPPERS.contains(qualifiedName)) {
            PsiType[] parameters = classType.getParameters();
            return arrayOf(parameters.length == 0 ? object() : create(parameters[0], depth, visiting));
        }
        if (InheritanceUtil.isInheritor(psiClass, "java.lang.Iterable")) {
            PsiType element = PsiUtil.extractIterableTypeParameter(classType, false);
            return arrayOf(element == null ? object() : create(element, depth, visiting));
        }
        if (InheritanceUtil.isInheritor(psiClass, "java.util.Map")) {
            return object();
        }
        if (psiClass.isEnum()) {
            return enumSchema(psiClass);
        }
        if (depth >= MAX_DEPTH || !visiting.add(qualifiedName)) {
            return object();
        }
        try {
            return new OasSchema.Builder(OasSchemaType.OBJECT).build(
                builder -> builder.setProperties(collectProperties(psiClass, resolveResult.getSubstitutor(), depth, visiting))
            );
        }
        finally {
            visiting.remove(qualifiedName);
        }
    }

    @RequiredReadAction
    private static Map<String, OasSchema> collectProperties(PsiClass psiClass, PsiSubstitutor substitutor, int depth, Set<String> visiting) {
        Map<String, OasSchema> properties = new LinkedHashMap<>();
        for (PsiRecordComponent component : psiClass.getRecordComponents()) {
            addProperty(properties, component.getName(), substitutor.substitute(component.getType()), depth, visiting);
        }
        for (PsiField field : psiClass.getAllFields()) {
            if (field instanceof PsiEnumConstant
                || field.hasModifierProperty(PsiModifier.STATIC)
                || field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }
            addProperty(properties, field.getName(), substitutor.substitute(field.getType()), depth, visiting);
        }
        return properties;
    }

    @RequiredReadAction
    private static void addProperty(Map<String, OasSchema> properties, @Nullable String name, @Nullable PsiType type, int depth, Set<String> visiting) {
        if (name == null || type == null || properties.containsKey(name)) {
            return;
        }
        OasSchema schema = create(type, depth + 1, visiting);
        if (schema != null) {
            properties.put(name, schema);
        }
    }

    @RequiredReadAction
    private static OasSchema enumSchema(PsiClass psiClass) {
        List<String> values = new ArrayList<>();
        for (PsiField field : psiClass.getFields()) {
            if (field instanceof PsiEnumConstant constant) {
                values.add(constant.getName());
            }
        }
        return new OasSchema(OasSchemaType.STRING, null, null, null, null, null, values, null, false, false);
    }

    private static @Nullable OasSchema simple(String name) {
        return switch (name) {
            case "int", "java.lang.Integer", "Integer", "short", "java.lang.Short", "Short", "byte", "java.lang.Byte", "Byte" ->
                schema(OasSchemaType.INTEGER, OasSchemaFormat.INT_32);
            case "long", "java.lang.Long", "Long" -> schema(OasSchemaType.INTEGER, OasSchemaFormat.INT_64);
            case "java.math.BigInteger" -> schema(OasSchemaType.INTEGER, null);
            case "float", "java.lang.Float", "Float" -> schema(OasSchemaType.NUMBER, OasSchemaFormat.FLOAT);
            case "double", "java.lang.Double", "Double" -> schema(OasSchemaType.NUMBER, OasSchemaFormat.DOUBLE);
            case "java.math.BigDecimal", "java.lang.Number", "Number" -> schema(OasSchemaType.NUMBER, null);
            case "boolean", "java.lang.Boolean", "Boolean" -> schema(OasSchemaType.BOOLEAN, null);
            case "char", "java.lang.Character", "Character", "java.lang.String", "String", "java.lang.CharSequence", "CharSequence" ->
                schema(OasSchemaType.STRING, null);
            case "java.util.UUID" -> schema(OasSchemaType.STRING, OasSchemaFormat.UUID);
            case "java.time.LocalDate" -> schema(OasSchemaType.STRING, OasSchemaFormat.DATE);
            case "java.time.LocalTime", "java.time.OffsetTime" -> schema(OasSchemaType.STRING, OasSchemaFormat.PARTIAL_TIME);
            case "java.time.LocalDateTime", "java.time.OffsetDateTime", "java.time.ZonedDateTime", "java.time.Instant", "java.util.Date" ->
                schema(OasSchemaType.STRING, OasSchemaFormat.DATE_TIME);
            case "java.net.URI" -> schema(OasSchemaType.STRING, OasSchemaFormat.URI);
            case "java.net.URL" -> schema(OasSchemaType.STRING, OasSchemaFormat.URL);
            case "java.lang.Object", "Object" -> object();
            default -> null;
        };
    }

    private static OasSchema schema(OasSchemaType type, @Nullable OasSchemaFormat format) {
        return new OasSchema.Builder(type).build(builder -> builder.setFormat(format));
    }

    private static OasSchema object() {
        return new OasSchema.Builder(OasSchemaType.OBJECT).build();
    }

    private static OasSchema arrayOf(@Nullable OasSchema items) {
        return new OasSchema.Builder(OasSchemaType.ARRAY).build(builder -> builder.setItems(items != null ? items : object()));
    }
}
