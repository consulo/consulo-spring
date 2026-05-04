package com.intellij.spring.osgi.model;

import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.osgi.constants.SpringOsgiConstants;
import com.intellij.spring.osgi.model.xml.*;
import com.intellij.spring.osgi.model.xml.compendium.PropertyPlaceholder;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

public class SpringOsgiDomExtender extends DomExtender<Beans> {

    public void registerExtensions(@NotNull final Beans element, @NotNull final DomExtensionsRegistrar registrar) {
      registrar.registerCollectionChildrenExtension(new XmlName("service", SpringOsgiConstants.OSGI_NAMESPACE), Service.class);
      registrar.registerCollectionChildrenExtension(new XmlName("reference", SpringOsgiConstants.OSGI_NAMESPACE), Reference.class);
      registrar.registerCollectionChildrenExtension(new XmlName("list", SpringOsgiConstants.OSGI_NAMESPACE), List.class);
      registrar.registerCollectionChildrenExtension(new XmlName("set", SpringOsgiConstants.OSGI_NAMESPACE), Set.class);
      registrar.registerCollectionChildrenExtension(new XmlName("bundle", SpringOsgiConstants.OSGI_NAMESPACE), Bundle.class);

      //registrar.registerAttributeChildExtension(new XmlName("default-timeout", SpringOsgiConstants.OSGI_NAMESPACE), DefaultTimeout.class  );

      //todo register all spring-osgi-compendium.xsd extentions
      registrar.registerCollectionChildrenExtension(new XmlName("property-placeholder", SpringOsgiConstants.OSGI_COMPENDIUM_NAMESPACE), PropertyPlaceholder.class);
    }
}
