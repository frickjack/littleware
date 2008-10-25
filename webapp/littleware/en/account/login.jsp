<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>


<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom documentation entry -->
  <entry>
    <title> /en/account/login.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/account/login.jsp"/>
    <summary>Simple login page - manages login via the lw_session JSF bean.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="account" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_account.jspf" charEncoding="UTF-8" />
</gen:websupport>

<gen:document 
      title="Login"
      last_modified="02/07/2007"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >
<f:view>
<f:loadBundle basename="littleware.web.messages.Messages" var="lw_msgs" />

<h3> Login </h3>

          <c:if test="${lw_login.lastResult != null && lw_login.lastResult != 'Ok'}">
              <hr />
               <font color="red">
               <h:outputText  id="trick1" value="#{lw_msgs.error}" escape="false" />:
               <h:outputText id="trick2" value="#{lw_login.lastResult}" escape="false" />
               </font>
              <hr />
          </c:if>

<c:choose>
<c:when test="${lw_user.guest}">
<h:form id="login">
   <table width="100%" class="formtable">
    <tr>
       <td class="formtable"> 
          <h:outputText id="username"  value="#{lw_msgs.username}" escape="false" />
         </td>
       <td class="formtable"> <h:inputText value="#{lw_login.name}" size="40" /></td>
    </tr>
    <tr>
       <td class="formtable"> 
          <h:outputText id="password" value="#{lw_msgs.password}" escape="false" />
         </td>
       <td class="formtable"> <h:inputSecret value="#{lw_login.password}" size="40" maxlength="40" /></td>
    </tr>
  </table>

  <p>
     <h:commandButton value="Login" action="#{lw_login.authenticateAction}" />
  </p>
</h:form>
</c:when>
<c:otherwise>
  <p>
  You are logged in as <em><c:out value="${lw_user.user.name}" /></em>.  
Please <a href="logout.jsf">logout</a> to login as another user.
</p>
</c:otherwise>
</c:choose>

</f:view>
</gen:document>

</gen:view>
