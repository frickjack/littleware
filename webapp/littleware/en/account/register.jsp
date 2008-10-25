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
    <title> /en/account/register.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/account/register.jsp"/>
    <summary>Register a new user.
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
      title="Home Index"
      last_modified="11/19/2006"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Littleware Registration </h3>

<p>
Thank you for setting up an account with the littleware web portal.
Once your account is setup you will have wider access to 
to the site including access to source code, blogs,
online orders, web services,
and account management tools.
</p>
<p>
Please fill out and submit the form below, 
and the backend will send a password to the supplied
e-mail address.
</p>

<f:view>
<f:loadBundle basename="littleware.web.messages.Messages" var="lw_msgs" />

    
    <c:choose>
        <c:when test="${lw_register.lastResult == null}" />
        <c:when test="${lw_register.lastResult != 'Ok'}">
              <hr />
               <font color="red">
               <h:outputText  id="trick1" value="#{lw_msgs.error}" escape="false" />:
               <h:outputText id="trick2" value="#{lw_register.lastResult}" escape="false" />
               </font>
              <hr />
          </c:when>
          <c:otherwise>
              <hr />
              <p style="color:#00ff00;">Password e-mail sent to last user registered!</p>
          </c:otherwise>
      </c:choose>
      
    <h:form>
      <table border="0">
        <tr>
           <td> 
               <h:outputText  value="#{lw_msgs.username}" escape="false" />
              </td>
           <td> <h:inputText value="#{lw_register.name}" /></td>
        </tr>
        <tr>
           <td> 
                e-mail 
                </td>
           <td> <h:inputText value="#{lw_register.email}" /></td>
        </tr>
      </table>
       <br />
         <h:commandButton value="Register" action="#{lw_register.newUserAction}" />
    </h:form>

</f:view>

</gen:document>
</gen:view>
