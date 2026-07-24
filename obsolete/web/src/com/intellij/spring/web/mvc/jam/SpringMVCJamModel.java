package com.intellij.spring.web.mvc.jam;

import com.intellij.jam.JamService;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleServiceManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.search.GlobalSearchScope;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCJamModel {
  private final Module myModule;


  public static SpringMVCJamModel getModel(@NotNull Module module) {
    return ModuleServiceManager.getService(module, SpringMVCJamModel.class);
  }

  public SpringMVCJamModel(@NotNull Module module) {
    myModule = module;
  }


  public List<SpringMVCRequestMapping> getRequestMappings() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);
    List<SpringMVCRequestMapping> result = new ArrayList<SpringMVCRequestMapping>();
    result.addAll(service.getJamClassElements(SpringMVCRequestMapping.ClassMapping.META, SpringMVCRequestMapping.REQUEST_MAPPING, scope));
    result.addAll(service.getJamMethodElements(SpringMVCRequestMapping.MethodMapping.META, SpringMVCRequestMapping.REQUEST_MAPPING, scope));
    return result;
  }

  public Collection<SpringMVCModelAttribute> getModelAttributes() {
    List<SpringMVCRequestMapping> list = getRequestMappings();
    Set<SpringMVCModelAttribute> attributes = new THashSet<SpringMVCModelAttribute>(new TObjectHashingStrategy<SpringMVCModelAttribute>() {
      public int computeHashCode(SpringMVCModelAttribute object) {
        String name = object.getName();
        return name == null ? 0 : name.hashCode();
      }

      public boolean equals(SpringMVCModelAttribute o1, SpringMVCModelAttribute o2) {
        return Comparing.equal(o1.getName(), o2.getName()) && Comparing.equal(o1.getType(), o2.getType());
      }
    });
    for (SpringMVCRequestMapping mapping : list) {
      attributes.addAll(mapping.getModelAttributes());
    }
    return attributes;
  }
}
