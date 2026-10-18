package consulo.spring.impl.web.endpoint;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.application.ReadAction;
import consulo.application.util.concurrent.AppExecutorUtil;
import consulo.endpoint.EndpointDataKeys;
import consulo.endpoint.EndpointElementItem;
import consulo.endpoint.EndpointListItem;
import consulo.endpoint.url.Authority;
import consulo.endpoint.url.UrlConstants;
import consulo.endpoint.url.UrlTargetInfo;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.CopyPasteManager;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 2026-10-18
 */
@ActionImpl(id = "Spring.CopyEndpointUrl", parents = @ActionParentRef(@ActionRef(id = "Endpoints.ContextMenu")))
public final class SpringCopyEndpointUrlAction extends DumbAwareAction implements AnActionWithSyncUpdate {
    private static final Logger LOG = Logger.getInstance(SpringCopyEndpointUrlAction.class);

    public SpringCopyEndpointUrlAction() {
        super(SpringLocalize.webActionCopyUrlText(), LocalizeValue.empty(), PlatformIconGroup.actionsCopy());
    }

    @Override
    public void update(AnActionEvent e) {
        List<EndpointListItem> items = e.getData(EndpointDataKeys.SELECTED_ITEMS);
        boolean available = false;
        if (items != null) {
            for (EndpointListItem item : items) {
                if (item instanceof EndpointElementItem<?, ?> element && element.getProvider() instanceof SpringEndpointProviderBase<?>) {
                    available = true;
                    break;
                }
            }
        }
        e.getPresentation().setEnabledAndVisible(available);
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        List<EndpointListItem> items = e.getData(EndpointDataKeys.SELECTED_ITEMS);
        if (items == null || items.isEmpty()) {
            return;
        }
        List<EndpointListItem> selected = List.copyOf(items);
        UIAccess uiAccess = UIAccess.current();
        CompletableFuture.supplyAsync(() -> ReadAction.compute(() -> collectUrls(selected)), AppExecutorUtil.getAppExecutorService())
            .thenAcceptAsync(text -> {
                if (!text.isEmpty()) {
                    CopyPasteManager.getInstance().setText(text);
                }
            }, uiAccess)
            .whenComplete((ignored, error) -> {
                if (error != null) {
                    LOG.error(error);
                }
            });
    }

    @RequiredReadAction
    private static String collectUrls(List<EndpointListItem> items) {
        Set<String> urls = new LinkedHashSet<>();
        for (EndpointListItem item : items) {
            if (!(item instanceof EndpointElementItem<?, ?> element)
                || !(element.getProvider() instanceof SpringEndpointProviderBase<?>)
                || !element.isValid()) {
                continue;
            }
            Iterable<UrlTargetInfo> targets = element.getUrlTargetInfos();
            if (targets == null) {
                continue;
            }
            for (UrlTargetInfo target : targets) {
                if (target instanceof SpringUrlTargetInfoBase springTarget) {
                    urls.add(toUrl(springTarget));
                }
            }
        }
        return String.join("\n", urls);
    }

    private static String toUrl(SpringUrlTargetInfoBase target) {
        String authority = getAuthority(target);
        if (authority == null) {
            return target.getPathText();
        }
        String scheme = target.getSchemes().isEmpty() ? UrlConstants.HTTP_SCHEME : target.getSchemes().get(0);
        return scheme + authority + target.getPathText();
    }

    private static @Nullable String getAuthority(UrlTargetInfo target) {
        for (Authority authority : target.getAuthorities()) {
            if (authority instanceof Authority.Exact exact) {
                return exact.getText();
            }
        }
        return null;
    }
}
