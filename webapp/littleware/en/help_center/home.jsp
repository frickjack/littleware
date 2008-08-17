<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/help_center/home.xsl" ?>

<%@ page contentType="text/xml" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="help" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_help_center.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="home" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Home Index"
      last_modified="01/25/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
     >
<h3> Hello! </h3>

<p>
Welcome to the Littleware help center.
The help center is still under construction.
</p>

</gen:document>
</gen:view>
