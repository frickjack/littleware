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
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="account" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_account.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="update" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Change Password"
      last_modified="01/03/2007"
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

<h3> Update Info </h3>

<c:if test="${lw_user.authenticatedName == null}">
    <c:redirect url="login.jsf" />
</c:if>
          <c:if test="${lw_user.error != null}">
              <hr />
               <font color="red">
               <h:outputText  id="trick1" value="#{lw_msgs.error}" escape="false" />:
               <h:outputText id="trick2" value="#{lw_user.error}" escape="false" />
               </font>
              <hr />
          </c:if>

<h:form id="updateContact">
   <table width="100%" class="formtable">
    <tr>
       <td class="formtable"> 
          <h:outputText  value="#{lw_msgs.username}" escape="false" />
         </td>
       <td class="formtable"> <h:outputText value="#{lw_user.authenticatedName}" /></td>
    </tr>
    <tr>
       <td class="formtable"> 
          email
         </td>
       <td class="formtable"> <h:inputText id="email" value="#{lw_user.email}" /></td>
    </tr>
  </table>

  <p>
     <h:commandButton value="Submit" action="#{lw_user.updateContactAction}" 
                />
  </p>
</h:form>


</f:view>

</gen:document>

</gen:view>
