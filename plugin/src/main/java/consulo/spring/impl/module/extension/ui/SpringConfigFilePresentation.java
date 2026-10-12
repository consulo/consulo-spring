package consulo.spring.impl.module.extension.ui;

import consulo.localize.LocalizeValue;
import consulo.ui.TextAttribute;
import consulo.ui.TextItemPresentation;
import consulo.ui.image.Image;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
record SpringConfigFilePresentation(@Nullable Image icon, String name, String location, LocalizeValue problem) {
    SpringConfigFilePresentation(@Nullable Image icon, String name, String location) {
        this(icon, name, location, LocalizeValue.empty());
    }

    void render(TextItemPresentation presentation) {
        boolean valid = problem.isEmpty();

        presentation.withIcon(icon);
        presentation.append(name, valid ? TextAttribute.REGULAR : TextAttribute.ERROR);
        if (!location.isEmpty()) {
            presentation.append(" (" + location + ")", valid ? TextAttribute.GRAYED : TextAttribute.ERROR);
        }
        if (!valid) {
            presentation.withTooltip(problem);
        }
    }
}
