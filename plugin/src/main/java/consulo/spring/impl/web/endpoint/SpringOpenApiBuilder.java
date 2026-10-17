package consulo.spring.impl.web.endpoint;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiType;
import consulo.annotation.access.RequiredReadAction;
import consulo.endpoint.mime.MimeTypeConstants;
import consulo.endpoint.oas.OasEndpointPath;
import consulo.endpoint.oas.OasMediaTypeObject;
import consulo.endpoint.oas.OasOperation;
import consulo.endpoint.oas.OasParameter;
import consulo.endpoint.oas.OasParameterIn;
import consulo.endpoint.oas.OasRequestBody;
import consulo.endpoint.oas.OasResponse;
import consulo.endpoint.oas.OasSchema;
import consulo.endpoint.oas.OasSchemaType;
import consulo.endpoint.oas.OpenApiSpecification;
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
public final class SpringOpenApiBuilder {
    private static final String DEFAULT_METHOD = "GET";
    private static final String SUCCESS_CODE = "200";
    private static final String SUCCESS_DESCRIPTION = "OK";
    private static final String TEXT_HTML = "text/html";
    private static final String HTTP_HEADERS = "org.springframework.http.HttpHeaders";
    private static final Set<String> BODY_RETURN_TYPES = Set.of("org.springframework.http.ResponseEntity", "org.springframework.http.HttpEntity");
    private static final int MAX_META_DEPTH = 4;

    private SpringOpenApiBuilder() {
    }

    @RequiredReadAction
    public static @Nullable OpenApiSpecification build(SpringUrlTargetInfoBase target) {
        if (!(target.resolveToPsiElement() instanceof PsiMethod method)) {
            return null;
        }
        PsiAnnotation annotation = target.getAnnotation();
        String httpMethod = target.getMethods().isEmpty() ? DEFAULT_METHOD : target.getMethods().iterator().next();
        String absolutePath = new OasEndpointPath.Builder(target.getPath()).build().getAbsolutePath();
        String summary = httpMethod + " " + absolutePath;

        List<OasParameter> parameters = buildParameters(method, target.getPathText());
        OasRequestBody requestBody = buildRequestBody(method, target);
        List<OasResponse> responses = List.of(buildResponse(method, annotation, target.isClient()));
        PsiClass containingClass = method.getContainingClass();
        String tag = containingClass == null ? null : containingClass.getName();

        OasOperation operation = new OasOperation.Builder(httpMethod).build(builder -> {
            builder.setSummary(summary);
            builder.setOperationId(method.getName());
            builder.setTags(tag == null ? List.of() : List.of(tag));
            builder.setDeprecated(target.isDeprecated());
            builder.setParameters(parameters);
            builder.setRequestBody(requestBody);
            builder.setResponses(responses);
        });
        OasEndpointPath path = new OasEndpointPath.Builder(target.getPath()).build(builder -> {
            builder.setSummary(summary);
            builder.setOperations(List.of(operation));
        });
        return new OpenApiSpecification(List.of(path));
    }

    @RequiredReadAction
    private static List<OasParameter> buildParameters(PsiMethod method, String pathText) {
        Map<String, PsiParameter> pathParameters = new LinkedHashMap<>();
        List<OasParameter> result = new ArrayList<>();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiAnnotation pathVariable = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.PATH_VARIABLE);
            if (pathVariable != null) {
                String name = SpringRequestMappingSearch.getParameterBindingName(parameter, pathVariable);
                if (name != null) {
                    pathParameters.putIfAbsent(name, parameter);
                }
                continue;
            }
            addParameter(result, parameter, SpringWebConstants.REQUEST_PARAM, OasParameterIn.QUERY);
            addParameter(result, parameter, SpringWebConstants.REQUEST_HEADER, OasParameterIn.HEADER);
            addParameter(result, parameter, SpringWebConstants.COOKIE_VALUE, OasParameterIn.COOKIE);
        }

        List<OasParameter> pathResult = new ArrayList<>();
        for (String name : SpringUriTemplateUtil.names(pathText)) {
            PsiParameter parameter = pathParameters.get(name);
            OasSchema schema = parameter == null ? null : SpringOasSchemaFactory.create(parameter.getType());
            pathResult.add(new OasParameter.Builder(name, OasParameterIn.PATH).build(builder -> {
                builder.setRequired(true);
                builder.setSchema(schema != null ? schema : new OasSchema.Builder(OasSchemaType.STRING).build());
            }));
        }
        pathResult.addAll(result);
        return pathResult;
    }

    @RequiredReadAction
    private static void addParameter(List<OasParameter> result, PsiParameter parameter, String annotationName, OasParameterIn in) {
        PsiAnnotation annotation = SpringRequestMappingSearch.findParameterAnnotation(parameter, annotationName);
        if (annotation == null || SpringOasSchemaFactory.isBinary(parameter.getType()) || isType(parameter.getType(), HTTP_HEADERS)) {
            return;
        }
        String name = SpringRequestMappingSearch.getParameterBindingName(parameter, annotation);
        if (name == null) {
            return;
        }
        PsiType type = SpringOasSchemaFactory.unwrap(parameter.getType());
        boolean required = isRequired(annotation) && annotation.findDeclaredAttributeValue("defaultValue") == null && type == parameter.getType();
        OasSchema schema = SpringOasSchemaFactory.create(type);
        result.add(new OasParameter.Builder(name, in).build(builder -> {
            builder.setRequired(required);
            builder.setSchema(schema);
        }));
    }

    @RequiredReadAction
    private static @Nullable OasRequestBody buildRequestBody(PsiMethod method, SpringUrlTargetInfoBase target) {
        Map<String, OasSchema> multipart = new LinkedHashMap<>();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiAnnotation body = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.REQUEST_BODY);
            if (body != null) {
                OasSchema schema = SpringOasSchemaFactory.create(parameter.getType());
                Map<String, OasSchema> content = new LinkedHashMap<>();
                for (String contentType : contentTypes(target.getContentTypes(), MimeTypeConstants.APPLICATION_JSON)) {
                    content.put(contentType, schema != null ? schema : new OasSchema.Builder(OasSchemaType.OBJECT).build());
                }
                return new OasRequestBody(content, isRequired(body));
            }

            PsiAnnotation part = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.REQUEST_PART);
            PsiAnnotation param = SpringRequestMappingSearch.findParameterAnnotation(parameter, SpringWebConstants.REQUEST_PARAM);
            PsiAnnotation partAnnotation = part != null ? part : param;
            if (partAnnotation != null && (part != null || SpringOasSchemaFactory.isBinary(parameter.getType()))) {
                String name = SpringRequestMappingSearch.getParameterBindingName(parameter, partAnnotation);
                OasSchema schema = SpringOasSchemaFactory.create(parameter.getType());
                if (name != null && schema != null) {
                    multipart.put(name, schema);
                }
            }
        }
        if (multipart.isEmpty()) {
            return null;
        }
        OasSchema schema = new OasSchema.Builder(OasSchemaType.OBJECT).build(builder -> builder.setProperties(multipart));
        return new OasRequestBody(Map.of(MimeTypeConstants.MULTIPART_FORM_DATA, schema), true);
    }

    @RequiredReadAction
    private static OasResponse buildResponse(PsiMethod method, @Nullable PsiAnnotation annotation, boolean client) {
        PsiType returnType = method.getReturnType();
        if (returnType == null || SpringOasSchemaFactory.isVoid(returnType)) {
            return new OasResponse(SUCCESS_CODE, SUCCESS_DESCRIPTION);
        }
        PsiType unwrapped = SpringOasSchemaFactory.unwrap(returnType);
        if (!client && !returnsBody(method, returnType)) {
            OasSchema html = new OasSchema.Builder(OasSchemaType.STRING).build();
            return new OasResponse(SUCCESS_CODE, SUCCESS_DESCRIPTION, Map.of(TEXT_HTML, new OasMediaTypeObject(html, Map.of())));
        }
        OasSchema schema = SpringOasSchemaFactory.create(unwrapped);
        if (schema == null) {
            return new OasResponse(SUCCESS_CODE, SUCCESS_DESCRIPTION);
        }
        List<String> produces = annotation == null
            ? List.of()
            : SpringRequestMappingSearch.readStrings(annotation, client && SpringClientSearch.isExchangeAnnotation(annotation) ? "accept" : "produces");
        Map<String, OasMediaTypeObject> content = new LinkedHashMap<>();
        for (String contentType : contentTypes(produces, MimeTypeConstants.APPLICATION_JSON)) {
            content.put(contentType, new OasMediaTypeObject(schema, Map.of()));
        }
        return new OasResponse(SUCCESS_CODE, SUCCESS_DESCRIPTION, content);
    }

    @RequiredReadAction
    private static boolean returnsBody(PsiMethod method, PsiType returnType) {
        if (returnType instanceof PsiClassType classType) {
            PsiClass psiClass = classType.resolve();
            if (psiClass != null && BODY_RETURN_TYPES.contains(psiClass.getQualifiedName())) {
                return true;
            }
        }
        if (hasAnnotation(method, SpringWebConstants.RESPONSE_BODY, 0, new HashSet<>())) {
            return true;
        }
        PsiClass containingClass = method.getContainingClass();
        return containingClass != null && hasAnnotation(containingClass, SpringWebConstants.RESPONSE_BODY, 0, new HashSet<>());
    }

    @RequiredReadAction
    private static boolean hasAnnotation(PsiModifierListOwner owner, String qualifiedName, int depth, Set<String> visited) {
        PsiModifierList modifierList = owner.getModifierList();
        if (modifierList == null || depth > MAX_META_DEPTH) {
            return false;
        }
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
            String name = annotation.getQualifiedName();
            if (name == null || name.startsWith("java.lang.annotation.") || !visited.add(name)) {
                continue;
            }
            if (qualifiedName.equals(name) || SpringWebConstants.REST_CONTROLLER.equals(name)) {
                return true;
            }
            PsiClass annotationType = annotation.resolveAnnotationType();
            if (annotationType != null && hasAnnotation(annotationType, qualifiedName, depth + 1, visited)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isRequired(PsiAnnotation annotation) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("required");
        return !(value instanceof PsiLiteralExpression literal) || !Boolean.FALSE.equals(literal.getValue());
    }

    private static boolean isType(PsiType type, String qualifiedName) {
        return type instanceof PsiClassType classType && qualifiedName.equals(classType.rawType().getCanonicalText());
    }

    private static List<String> contentTypes(Iterable<String> declared, String fallback) {
        List<String> result = new ArrayList<>();
        for (String contentType : declared) {
            if (!contentType.startsWith("!")) {
                result.add(contentType);
            }
        }
        return result.isEmpty() ? List.of(fallback) : result;
    }
}
