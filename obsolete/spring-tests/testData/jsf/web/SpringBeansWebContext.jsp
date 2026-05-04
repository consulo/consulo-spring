<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<f:view>
  <h:form>
    <h:commandButton action="#{webApplicationContext.<warning>unknown</warning>}"/>
    <h:commandButton action="#{webApplicationContext.springBean}"/>
    <h:commandButton action="#{webApplicationContext.aliasName}"/>
    <h:commandButton action="#{webApplicationContext.javaConfiguredBean}"/>
    <h:commandButton action="#{webApplicationContext.javaConfiguredBeanAlias}"/>
    <h:commandButton action="#{webApplicationContext.<warning>javaConfiguredPrivateBean</warning>}"/>
    <h:commandButton action="#{webApplicationContext.<warning>javaConfiguredProtectedBean</warning>}"/>
  </h:form>
</f:view>