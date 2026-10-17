package consulo.spring.impl.web.endpoint;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.util.ModificationTracker;
import consulo.component.util.SimpleModificationTracker;
import consulo.module.content.ProjectRootManager;
import consulo.project.DumbService;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 2026-10-17
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public final class SpringWebModificationTracker implements ModificationTracker {
    private final SimpleModificationTracker myStructure = new SimpleModificationTracker();
    private final Project myProject;

    @Inject
    public SpringWebModificationTracker(Project project) {
        myProject = project;
    }

    public static SpringWebModificationTracker getInstance(Project project) {
        return project.getInstance(SpringWebModificationTracker.class);
    }

    public void incStructure() {
        myStructure.incModificationCount();
    }

    @Override
    public long getModificationCount() {
        return myStructure.getModificationCount()
            + ProjectRootManager.getInstance(myProject).getModificationCount()
            + DumbService.getInstance(myProject).getModificationTracker().getModificationCount();
    }
}
