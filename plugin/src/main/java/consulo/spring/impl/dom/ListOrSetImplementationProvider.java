package consulo.spring.impl.dom;

import com.intellij.spring.impl.model.beans.ListOrSetImpl;
import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import consulo.xml.dom.DomElementImplementationProvider;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 04/02/2023
 */
public class ListOrSetImplementationProvider implements DomElementImplementationProvider<ListOrSet, ListOrSetImpl> {
  @Nonnull
  @Override
  public Class<ListOrSet> getInterfaceClass() {
    return ListOrSet.class;
  }

  @Nonnull
  @Override
  public Class<ListOrSetImpl> getImplementationClass() {
    return ListOrSetImpl.class;
  }
}
