package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.spring.webflow.util.WebflowUtil;
import java.util.function.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlowNameConverter extends ResolvingConverter<Flow> implements CustomReferenceConverter<Flow> {

  public Flow fromString(@Nullable @NonNls final String s, final ConvertContext context) {
     return WebflowUtil.findFlowByName(s, context.getModule());
  }

  public String toString(@Nullable final Flow flow, final ConvertContext context) {
    return WebflowUtil.getFlowName(flow, context.getModule());
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<Flow> flowGenericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {

    final Flow flow = flowGenericDomValue.getValue();
    if (flow == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[]{createFlowNameReference(flow, element, context)};
  }

  private static PsiReference createFlowNameReference(final Flow flow, final PsiElement element, final ConvertContext context) {
    final XmlFile currentFlowFile = DomUtil.getFile(flow);

    return new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        return WebflowUtil.resolveFlow(flow, context.getModule());
      }

      public Object[] getVariants() {
        return ContainerUtil.map2Array(getAllFlows(currentFlowFile, context.getModule()), new Function<Flow, Object>() {
          public Object fun(final Flow flow) {
            final String flowName = WebflowUtil.getFlowName(flow, context.getModule());
            return flowName == null ? "" : flowName;
          }
        });
      }

      public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
        if (resolve() instanceof XmlFile) {
          String name = getNameWithoutExtension(newElementName);
          return super.handleElementRename(name == null ? newElementName : name);
        }
        return super.handleElementRename(newElementName);
      }

      public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
        if (element instanceof XmlFile) {
          final VirtualFile file = ((XmlFile)element).getVirtualFile();
          if (file != null) {
            return super.handleElementRename(file.getNameWithoutExtension());
          }
        }
        return getElement();
      }

      public String getNameWithoutExtension(final String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) return fileName;
        return fileName.substring(0, index);
      }

    };
  }

  @NotNull
  public Collection<? extends Flow> getVariants(final ConvertContext context) {
    final Module module = context.getModule();

    return getAllFlows(context.getFile().getOriginalFile(), module);
  }

  private static List<Flow> getAllFlows(final PsiFile originalFile, final Module module) {
    return WebflowUtil.getAllFlows(module, Collections.singletonList(originalFile));
  }

}
