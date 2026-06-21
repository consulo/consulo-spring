package com.intellij.spring.impl.ide.usages;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.dataContext.DataSink;
import consulo.dataContext.UiDataProvider;
import consulo.language.editor.LangDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.spring.impl.SpringIcons;
import consulo.ui.image.Image;
import consulo.usage.Usage;
import consulo.usage.UsageGroup;
import consulo.usage.UsageInfo;
import consulo.usage.UsageView;
import consulo.usage.rule.PsiElementUsage;
import consulo.usage.rule.UsageGroupingRule;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.status.FileStatus;
import consulo.virtualFileSystem.status.FileStatusManager;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SpringBeansGroupingRule implements UsageGroupingRule {
    private static final Logger LOG = Logger.getInstance(SpringBeansGroupingRule.class);

    @Override
    public UsageGroup groupUsage(Usage usage) {
        if (usage instanceof PsiElementUsage) {
            PsiElement psiElement = ((PsiElementUsage) usage).getElement();

            final PsiFile psiFile = psiElement.getContainingFile();
            final Project project = psiElement.getProject();
            if (psiFile instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile) psiFile)) {
                final DomElement domElement = DomUtil.getDomElement(psiElement);
                if (domElement != null) {
                    final DomSpringBean springBean = domElement.getParentOfType(DomSpringBean.class, false);
                    if (springBean != null) {
                        return new SpringBeansUsageGroup(springBean);
                    }
                }
            }
        }
        return null;
    }

    private static class SpringBeansUsageGroup implements UsageGroup, UiDataProvider {
        private final String myName;
        private final DomSpringBean myBean;

        public SpringBeansUsageGroup(@Nonnull DomSpringBean bean) {
            myBean = bean;
            final String beanName = bean.getPresentation().getElementName();
            myName = beanName == null ? SpringBundle.message("spring.bean.with.unknown.name") : beanName;

            update();
        }

        @Override
        public void update() {
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final SpringBeansUsageGroup that = (SpringBeansUsageGroup) o;

            if (!myBean.equals(that.myBean)) {
                return false;
            }
            if (!myName.equals(that.myName)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            result = myName.hashCode();
            result = 31 * result + myBean.hashCode();
            return result;
        }

        @Override
        public Image getIcon() {
            return SpringIcons.SpringBean;
        }

        @Override
        @Nonnull
        public String getText(UsageView view) {
            return myName;
        }

        @Nonnull
        public DomSpringBean getBean() {
            return myBean;
        }

        @Override
        public FileStatus getFileStatus() {
            return isValid() ? FileStatusManager.getInstance(myBean.getPsiManager().getProject())
                .getStatus(DomUtil.getFile(getBean()).getVirtualFile()) : null;
        }

        @Override
        public boolean isValid() {
            return getBean().isValid();
        }

        @Override
        public void navigate(boolean focus) throws UnsupportedOperationException {
            if (canNavigate()) {
                SpringUtils.navigate(myBean);
            }
        }

        @Override
        public boolean canNavigate() {
            return isValid();
        }

        @Override
        public boolean canNavigateToSource() {
            return canNavigate();
        }

        @Override
        public int compareTo(UsageGroup usageGroup) {
            if (!(usageGroup instanceof SpringBeansUsageGroup)) {
                LOG.error("MethodUsageGroup expected but " + usageGroup.getClass() + " found");
            }

            return myName.compareTo(((SpringBeansUsageGroup) usageGroup).myName);
        }

        @Override
        public void uiDataSnapshot(DataSink sink) {
            sink.lazy(LangDataKeys.PSI_ELEMENT, this::getPsiElement);
            sink.lazy(UsageView.USAGE_INFO_KEY, () -> {
                PsiElement element = getPsiElement();
                return element != null ? new UsageInfo(element) : null;
            });
        }

        @Nullable
        private XmlElement getPsiElement() {
            return getBean().getXmlElement();
        }
    }
}
