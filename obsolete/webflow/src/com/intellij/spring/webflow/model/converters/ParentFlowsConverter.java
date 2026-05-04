package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.ArrayUtil;
import java.util.function.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.converters.DelimitedListConverter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParentFlowsConverter extends DelimitedListConverter<Flow> {
  public ParentFlowsConverter() {
    super(", ");
  }

  protected Flow convertString(final @Nullable String string, final ConvertContext context) {
    return WebflowUtil.findFlowByName(string, context.getModule());
  }

  protected String toString(@Nullable final Flow flow) {
    return WebflowUtil.getFlowName(flow);
  }

  protected Object[] getReferenceVariants(final ConvertContext context, final GenericDomValue<List<Flow>> listGenericDomValue) {
    final List<Flow> flows = listGenericDomValue.getValue();

    if (flows != null) {
      final Module module = context.getModule();

      final List<PsiFile> exceptedFiles = ContainerUtil.mapNotNull(flows, new Function<Flow, PsiFile>() {
        public PsiFile fun(final Flow flow) {
          return DomUtil.getFile(flow);
        }
      });
      exceptedFiles.add(context.getFile().getOriginalFile());

      List<Flow> all = WebflowUtil.getAllFlows(module, exceptedFiles);


      final List<String> names = ContainerUtil.mapNotNull(all, new Function<Flow, String>() {
        public String fun(final Flow flow) {
          return WebflowUtil.getFlowName(flow, context.getModule());
        }
      });
      return ArrayUtil.toStringArray(names);
    }

    return new Object[0];
  }

  protected PsiElement resolveReference(@Nullable final Flow flow, final ConvertContext context) {
    return flow == null ? null : WebflowUtil.resolveFlow(flow, context.getModule());
  }

  protected String getUnresolvedMessage(final String value) {
    return WebflowBundle.message("cannot.find.flow", value);
  }

  @Override
  protected PsiElement referenceBindToElement(final PsiReference psiReference,
                                              final PsiElement element,
                                              final Function<PsiElement, PsiElement> superBindToElementFunction,
                                              final Function<String, PsiElement> superElementRenameFunction)
      throws IncorrectOperationException {
    if (element instanceof XmlFile) {
      final VirtualFile file = ((XmlFile)element).getVirtualFile();
      if (file != null) {
        return referenceHandleElementRename(psiReference, file.getNameWithoutExtension(), superElementRenameFunction);
      }
    }
    return psiReference.getElement();
  }

  @Override
  protected PsiElement referenceHandleElementRename(final PsiReference psiReference,
                                                    final String newName,
                                                    final Function<String, PsiElement> superHandleElementRename)
      throws IncorrectOperationException {

    if (psiReference.resolve() instanceof XmlFile) {
      final String name = getNameWithoutExtension(newName);
      return superHandleElementRename.fun(name == null ? newName : name);
    }
    return superHandleElementRename.fun(newName);
  }

  private static String getNameWithoutExtension(final String fileName) {
    int index = fileName.lastIndexOf('.');
    if (index < 0) return fileName;
    return fileName.substring(0, index);
  }
}
