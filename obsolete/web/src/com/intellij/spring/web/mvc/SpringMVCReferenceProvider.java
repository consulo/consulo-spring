package com.intellij.spring.web.mvc;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.javaee.web.CustomServletReferenceAdapter;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.web.SpringWebBundle;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCReferenceProvider extends CustomServletReferenceAdapter {

  protected PsiReference[] createReferences(@NotNull PsiElement element,
                                            int offset,
                                            String text,
                                            @Nullable ServletMappingInfo info,
                                            boolean soft) {

    SpringMVCModel model = SpringMVCModel.getModel(element);
    return model == null || model.getAllModels().isEmpty() ? PsiReference.EMPTY_ARRAY : new PsiReference[] { new SpringMVCReference(element, offset, text, info, model.getWebFacet(),
                                                                                                                                    model.getSpringFacet(),
                                                                                                                                    soft)};
  }

  public PathReference createWebPath(String path, @NotNull PsiElement element, ServletMappingInfo info) {
    return null;
  }

  /**
 * @author Dmitry Avdeev
   */
  public static class SpringMVCReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider {

    private final ServletMappingInfo myInfo;
    private final WebFacet myWebFacet;
    private final SpringFacet mySpringFacet;

    public SpringMVCReference(PsiElement element,
                              int offset,
                              String text,
                              @Nullable ServletMappingInfo info,
                              WebFacet webFacet, SpringFacet springFacet, boolean soft) {

      super(element, new TextRange(offset, offset + text.length()), soft);
      myInfo = info;
      myWebFacet = webFacet;
      mySpringFacet = springFacet;
      if (info != null) {
        TextRange range = info.getNameRange(text);
        if (range != null) {
          setRangeInElement(range.shiftRight(offset));
        }
      }
    }

    @Nullable
    private SpringMVCModel getModel() {
      return SpringMVCModel.getModel(myWebFacet, mySpringFacet);
    }

    @Nullable
    private String getUrl() {
      String url = getValue();
      return myInfo != null ? myInfo.addMapping(url) : null;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      if (myInfo == null) {
        return super.handleElementRename(newElementName);
      } else {
        String s = myInfo.stripMapping(newElementName);
        return super.handleElementRename(s);
      }
    }

    public PsiElement resolve() {
      String url = getUrl();
      if (url == null) {
        return null;
      }
      SpringMVCModel model = getModel();
      if (model == null) {
        return null;
      }
      return model.resolveUrl(url);
    }

    public Object[] getVariants() {
      SpringMVCModel model = getModel();
      if (model == null) {
        return EMPTY_ARRAY;
      }
      return model.getAllUrls().toArray();
    }

    public String getUnresolvedMessagePattern() {
      return SpringWebBundle.message("cannot.resolve.controller.url", getValue());
    }
  }
}
