<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ page import="java.util.*,littleware.base.*,littleware.security.auth.*,littleware.security.*,littleware.asset.*,littleware.web.beans.*,littleware.web.pickle.*" %>
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
       <c:param name="sidemenu_select" value="home" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Home Index"
      last_modified="01/23/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
     >

<c:choose>
<c:when test="${lw_user.authenticatedName == null}">
    <h3> Must <a href="login.jsf">login</a> to view account data </h3>
</c:when>
<c:otherwise>

<h3> Hello, <c:out value="${lw_user.authenticatedName}" /> ! </h3>

<p>
Welcome to your Littleware account page.
The following services are available.
</p>
<ul>
<li> <a href="change_password.jsf"> Change password </a> </li>
<li> <a href="update_contact.jsf"> Update contact info </a> </li>
</ul>

<applet 
        id="toolbox"
        code="littleware.web.applet.Toolbox.class"
	width="400"
	height="150"
      >
    <param name="session_uuid" value="${lw_user.helper.session.objectId}" />
  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</applet>

<p> 
  Current session status: 
</p>

<%
   try {
      SessionBean   bean_user = (SessionBean) session.getAttribute( "lw_user" );
      SessionHelper m_helper = bean_user.getHelper ();
      AssetSearchManager m_search = m_helper.getService ( ServiceType.ASSET_SEARCH );
      LittleSession      a_session = m_helper.getSession ();
      PickleMaker<Asset>  pickle_asset = PickleType.XML.createPickleMaker ();

      pickle_asset.pickle ( a_session, out );
%>

<hr />
<p> 
  User asset:
</p>

<%
      pickle_asset.pickle ( a_session.getCreator ( m_search ), out );
%>
<hr />
<p> 
  User contact:
</p>
<%
      pickle_asset.pickle ( bean_user.getContact (), out );
    } catch ( Exception e ) {
        out.println ( "<gen:error> Caught unexpected: <br />" );
        out.println ( "<verbatim>\n\n" );
        out.println ( XmlSpecial.encode ( BaseException.getStackTrace ( e ) ) );
        out.println ( "\n\n</verbatim></gen:error>" );
    }
%>

</c:otherwise>
</c:choose>

</gen:document>

</gen:view>
