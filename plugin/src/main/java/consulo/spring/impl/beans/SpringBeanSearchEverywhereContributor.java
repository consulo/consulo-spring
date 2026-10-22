package consulo.spring.impl.beans;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicator;
import consulo.application.util.matcher.MinusculeMatcher;
import consulo.application.util.matcher.NameUtil;
import consulo.language.psi.PsiElement;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.searchEverywhere.SearchEverywhereContributor;
import consulo.searchEverywhere.SearchEverywhereItemPresentation;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.TextAttribute;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.ColoredListCellRenderer;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Predicate;

/**
 * The "Beans" tab of search everywhere: beans of the static Spring model, matched by name, alias or type.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public class SpringBeanSearchEverywhereContributor implements SearchEverywhereContributor<SpringBeanInfo> {
    public static final String ID = "SpringBeanSearchEverywhereContributor";

    private final Project myProject;

    public SpringBeanSearchEverywhereContributor(Project project) {
        myProject = project;
    }

    @Override
    public String getSearchProviderId() {
        return ID;
    }

    @Override
    public String getGroupName() {
        return SpringLocalize.beansSearchEverywhereGroup().get();
    }

    @Override
    public int getSortWeight() {
        return 900;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public boolean isShownInSeparateTab() {
        return true;
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    @Override
    public void fetchElements(String pattern, ProgressIndicator progressIndicator, Predicate<? super SpringBeanInfo> consumer) {
        if (pattern.isBlank()) {
            return;
        }

        MinusculeMatcher matcher = buildMatcher(pattern);
        List<SpringModuleBeans> modules = ReadAction.compute(() -> SpringBeansCollector.collect(myProject));
        for (SpringModuleBeans module : modules) {
            for (SpringContextInfo context : module.contexts()) {
                for (SpringBeanInfo bean : context.getBeans()) {
                    progressIndicator.checkCanceled();
                    if (matches(matcher, bean) && !consumer.test(bean)) {
                        return;
                    }
                }
            }
        }
    }

    private static MinusculeMatcher buildMatcher(String pattern) {
        return NameUtil.buildMatcher("*" + pattern.trim(), NameUtil.MatchingCaseSensitivity.NONE);
    }

    private static boolean matches(MinusculeMatcher matcher, SpringBeanInfo bean) {
        if (matcher.matches(bean.getName())) {
            return true;
        }
        for (String alias : bean.getAliases()) {
            if (matcher.matches(alias)) {
                return true;
            }
        }
        String type = bean.getShortTypeName();
        return type != null && matcher.matches(type);
    }

    @Override
    public boolean processSelectedItem(SpringBeanInfo selected, int modifiers, String searchText) {
        ReadAction.nonBlocking(() -> {
                PsiElement element = selected.getElement();
                return element != null && element.getNavigationElement() instanceof Navigatable navigatable ? navigatable : null;
            })
            .inSmartMode(myProject)
            .expireWith(myProject)
            .finishOnUiThread(Application::getDefaultModalityState, navigatable -> {
                if (navigatable != null && navigatable.canNavigate()) {
                    navigatable.navigate(true);
                }
            })
            .submitDefault();
        return true;
    }

    @Override
    public ListCellRenderer<? super SpringBeanInfo> getElementsRenderer() {
        return new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(JList<? extends SpringBeanInfo> list,
                                                 SpringBeanInfo value,
                                                 int index,
                                                 boolean selected,
                                                 boolean hasFocus) {
                setIcon(value.getKind().getIcon());
                append(value.getName(), value.isProfileActive()
                    ? SimpleTextAttributes.REGULAR_ATTRIBUTES
                    : SimpleTextAttributes.GRAYED_ATTRIBUTES);
                String type = value.getShortTypeName();
                if (type != null) {
                    append(" " + type, SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
                append("  ", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                append(SpringLocalize.beansLocation(value.getModuleName(), value.getContextName()), SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        };
    }

    @Override
    public SearchEverywhereItemPresentation getItemPresentation(SpringBeanInfo item, @Nullable MinusculeMatcher matcher) {
        SearchEverywhereItemPresentation.Builder builder = SearchEverywhereItemPresentation.builder()
            .icon(item.getKind().getIcon())
            .appendMatched(item.getName(), matcher, item.isProfileActive() ? TextAttribute.REGULAR : TextAttribute.GRAYED);
        String type = item.getShortTypeName();
        if (type != null) {
            builder.append(" " + type, TextAttribute.GRAY);
        }
        return builder
            .suffix(SpringLocalize.beansLocation(item.getModuleName(), item.getContextName()), null)
            .build();
    }

    @Override
    public @Nullable Object getDataForItem(SpringBeanInfo element, Key dataId) {
        return null;
    }
}
