<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<f:view>
  <h:form>
    <h:commandButton action="#{<warning>unknown</warning>}"/>
    <h:commandButton action="#{springBean}"/>
    <h:commandButton action="#{aliasName}"/>
    <h:commandButton action="#{javaConfiguredBean}"/>
    <h:commandButton action="#{javaConfiguredBeanAlias}"/>
    <h:commandButton action="#{<warning>javaConfiguredPrivateBean</warning>}"/>
    <h:commandButton action="#{<warning>javaConfiguredProtectedBean</warning>}"/>
  </h:form>
</f:view>