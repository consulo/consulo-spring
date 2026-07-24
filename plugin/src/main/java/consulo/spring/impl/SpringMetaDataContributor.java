package consulo.spring.impl;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.metadata.SpringBeanMetaData;
import com.intellij.spring.impl.ide.metadata.SpringStereotypeQualifierMetaData;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.filter.ElementFilter;
import consulo.language.psi.meta.MetaDataContributor;
import consulo.language.psi.meta.MetaDataRegistrar;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;

/**
 * @author VISTALL
 * @since 05/02/2023
 */
@ExtensionImpl
public class SpringMetaDataContributor implements MetaDataContributor {
  @Override
  public void contributeMetaData(MetaDataRegistrar metaDataRegistrar) {
    metaDataRegistrar.registerMetaData(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof XmlTag) {
          XmlTag tag = (XmlTag)element;
          DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
          if (!(domElement instanceof DomSpringBean)) {
            return false;
          }

          if (!(domElement instanceof CustomBeanWrapper)) {
            return true;
          }
          if (!((CustomBeanWrapper)domElement).isParsed()) {
            return true;
          }
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return XmlTag.class.isAssignableFrom(hintClass);
      }
    }, SpringBeanMetaData::new);

    metaDataRegistrar.registerMetaData(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof PsiAnnotation) {
          Module module = ModuleUtilCore.findModuleForPsiElement(context);
          if (module != null) {
            for (PsiClass psiClass : JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module)) {
              PsiAnnotation annotation = (PsiAnnotation)element;
              if (annotation.getQualifiedName().equals(psiClass.getQualifiedName())) {
                return true;
              }
            }
          }
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return PsiAnnotation.class.isAssignableFrom(hintClass);
      }
    }, SpringStereotypeQualifierMetaData::new);
  }
}
