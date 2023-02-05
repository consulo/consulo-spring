package consulo.spring.impl.boot;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import consulo.disposer.Disposable;
import com.intellij.spring.impl.ide.facet.SpringFileSet;

/**
 * @author VISTALL
 * @since 15-Jan-17
 */
public class SpringBootFileSet extends SpringFileSet {
  public SpringBootFileSet(@NonNls @Nonnull String id, @Nonnull String name, @Nonnull Disposable parent) {
    super(id, name, parent);
    setAutodetected(true);
  }

  public SpringBootFileSet(SpringFileSet original) {
    super(original);
    setAutodetected(true);
  }
}
