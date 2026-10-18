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

    public static final String BEAN = "org.springframework.context.annotation.Bean";
    public static final String ALIAS_FOR = "org.springframework.core.annotation.AliasFor";

    public static final String DISPATCHER_SERVLET = "org.springframework.web.servlet.DispatcherServlet";
    public static final String DISPATCHER_HANDLER = "org.springframework.web.reactive.DispatcherHandler";

    public static final String MVC_ROUTER_FUNCTION = "org.springframework.web.servlet.function.RouterFunction";
    public static final String WEBFLUX_ROUTER_FUNCTION = "org.springframework.web.reactive.function.server.RouterFunction";

    public static final String REPOSITORY = "org.springframework.data.repository.Repository";
    public static final String NO_REPOSITORY_BEAN = "org.springframework.data.repository.NoRepositoryBean";
    public static final String REPOSITORY_REST_RESOURCE = "org.springframework.data.rest.core.annotation.RepositoryRestResource";
    public static final String REST_RESOURCE = "org.springframework.data.rest.core.annotation.RestResource";

    public static final String ACTUATOR_ENDPOINT = "org.springframework.boot.actuate.endpoint.annotation.Endpoint";
    public static final String ACTUATOR_WEB_ENDPOINT = "org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint";
    public static final String ACTUATOR_SELECTOR = "org.springframework.boot.actuate.endpoint.annotation.Selector";

    public static final Map<String, String> ACTUATOR_OPERATIONS = Map.of(
        "org.springframework.boot.actuate.endpoint.annotation.ReadOperation", "GET",
        "org.springframework.boot.actuate.endpoint.annotation.WriteOperation", "POST",
        "org.springframework.boot.actuate.endpoint.annotation.DeleteOperation", "DELETE"
    );

    public static final String MESSAGE_MAPPING = "org.springframework.messaging.handler.annotation.MessageMapping";
    public static final String SUBSCRIBE_MAPPING = "org.springframework.messaging.simp.annotation.SubscribeMapping";
    public static final String MESSAGE_BROKER_REGISTRY = "org.springframework.messaging.simp.config.MessageBrokerRegistry";

    public static final String TILES_CONFIGURER = "org.springframework.web.servlet.view.tiles3.TilesConfigurer";

    public static final String SERVER_PORT = "server.port";
    public static final String DATA_REST_BASE_PATH = "spring.data.rest.base-path";
    public static final String MANAGEMENT_BASE_PATH = "management.endpoints.web.base-path";
    public static final String MANAGEMENT_EXPOSURE_INCLUDE = "management.endpoints.web.exposure.include";
    public static final String DEFAULT_MANAGEMENT_BASE_PATH = "/actuator";
    public static final String DEFAULT_MANAGEMENT_EXPOSURE = "health";
    public static final String SERVLET_CONTEXT_PATH = "server.servlet.context-path";
    public static final String MVC_SERVLET_PATH = "spring.mvc.servlet.path";
    public static final String WEBFLUX_BASE_PATH = "spring.webflux.base-path";

    public static final int DEFAULT_PORT = 8080;

    private SpringWebConstants() {
    }
}
