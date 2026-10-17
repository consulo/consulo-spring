package consulo.spring.impl.web.endpoint;

import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringWebConstants {
    public static final String CONTROLLER = "org.springframework.stereotype.Controller";
    public static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";

    public static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    public static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    public static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    public static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    public static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    public static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";

    public static final Map<String, String> COMPOSED_MAPPINGS = Map.of(
        GET_MAPPING, "GET",
        POST_MAPPING, "POST",
        PUT_MAPPING, "PUT",
        DELETE_MAPPING, "DELETE",
        PATCH_MAPPING, "PATCH"
    );

    public static final String FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient";

    public static final String HTTP_EXCHANGE = "org.springframework.web.service.annotation.HttpExchange";
    public static final String GET_EXCHANGE = "org.springframework.web.service.annotation.GetExchange";
    public static final String POST_EXCHANGE = "org.springframework.web.service.annotation.PostExchange";
    public static final String PUT_EXCHANGE = "org.springframework.web.service.annotation.PutExchange";
    public static final String DELETE_EXCHANGE = "org.springframework.web.service.annotation.DeleteExchange";
    public static final String PATCH_EXCHANGE = "org.springframework.web.service.annotation.PatchExchange";

    public static final Map<String, String> COMPOSED_EXCHANGES = Map.of(
        GET_EXCHANGE, "GET",
        POST_EXCHANGE, "POST",
        PUT_EXCHANGE, "PUT",
        DELETE_EXCHANGE, "DELETE",
        PATCH_EXCHANGE, "PATCH"
    );

    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    public static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
    public static final String REQUEST_HEADER = "org.springframework.web.bind.annotation.RequestHeader";
    public static final String COOKIE_VALUE = "org.springframework.web.bind.annotation.CookieValue";
    public static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    public static final String REQUEST_PART = "org.springframework.web.bind.annotation.RequestPart";
    public static final String RESPONSE_BODY = "org.springframework.web.bind.annotation.ResponseBody";
    public static final String MODEL_ATTRIBUTE = "org.springframework.web.bind.annotation.ModelAttribute";

    public static final String SERVER_PORT = "server.port";
    public static final String SERVLET_CONTEXT_PATH = "server.servlet.context-path";
    public static final String MVC_SERVLET_PATH = "spring.mvc.servlet.path";

    public static final int DEFAULT_PORT = 8080;

    private SpringWebConstants() {
    }
}
