package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.module.Module;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
public final class SpringWebServerSettings {
    public static final SpringWebServerSettings DEFAULT = new SpringWebServerSettings(SpringWebConstants.DEFAULT_PORT, "", "");

    private final int myPort;
    private final String myContextPath;
    private final String myServletPath;

    private SpringWebServerSettings(int port, String contextPath, String servletPath) {
        myPort = port;
        myContextPath = contextPath;
        myServletPath = servletPath;
    }

    @RequiredReadAction
    public static SpringWebServerSettings get(@Nullable Module module) {
        if (module == null) {
            return DEFAULT;
        }
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> CachedValueProvider.Result.create(
            compute(module),
            SpringWebModificationTracker.getInstance(module.getProject())
        ));
    }

    public int getPort() {
        return myPort;
    }

    public String getAuthority() {
        return "localhost:" + myPort;
    }

    public String applyTo(String path) {
        String prefix = SpringRequestMappingSearch.join(myContextPath, myServletPath);
        return "/".equals(prefix) ? path : SpringRequestMappingSearch.join(prefix, path);
    }

    @RequiredReadAction
    private static SpringWebServerSettings compute(Module module) {
        int port = SpringWebConstants.DEFAULT_PORT;
        String portText = SpringConfigValues.readValue(module, SpringWebConstants.SERVER_PORT);
        if (portText != null) {
            try {
                port = Integer.parseInt(portText.trim());
            }
            catch (NumberFormatException ignored) {
            }
        }
        String contextPath = SpringConfigValues.readValue(module, SpringWebConstants.SERVLET_CONTEXT_PATH);
        String servletPath = SpringConfigValues.readValue(module, SpringWebConstants.MVC_SERVLET_PATH);
        return new SpringWebServerSettings(
            port,
            contextPath == null ? "" : contextPath.trim(),
            servletPath == null ? "" : servletPath.trim()
        );
    }
}
