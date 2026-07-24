package consulo.spring.impl.toolWindow.tree.node;

import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.ui.ex.tree.AbstractTreeStructure;
import consulo.ui.ex.tree.NodeDescriptor;
import consulo.ui.ex.tree.TreeNode;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 2024-12-13
 */
public class SpringContextsTreeStructure extends AbstractTreeStructure {
    private final SpringContextsRootNode myRootNode;

    public SpringContextsTreeStructure(@Nonnull Project project) {
        myRootNode = new SpringContextsRootNode(project);
    }

    @Nonnull
    @Override
    public Object getRootElement() {
        return myRootNode;
    }

    @Nonnull
    @Override
    public Object[] getChildElements(@Nonnull Object element) {
        TreeNode<?> treeNode = (TreeNode) element;
        Collection<? extends TreeNode> elements = treeNode.getChildren();
        elements.forEach(node -> node.setParent(treeNode));
        return ArrayUtil.toObjectArray(elements);
    }

    @Override
    public boolean isValid(@Nonnull Object element) {
        return element instanceof AbstractTreeNode;
    }

    @Override
    public Object getParentElement(@Nonnull Object element) {
        if (element instanceof TreeNode) {
            return ((TreeNode) element).getParent();
        }
        return null;
    }

    @Override
    @Nonnull
    public NodeDescriptor createDescriptor(@Nonnull Object element, NodeDescriptor parentDescriptor) {
        return (NodeDescriptor) element;
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }
}
