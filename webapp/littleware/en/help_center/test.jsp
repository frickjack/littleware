<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/help_center/home_view.jsf?side_select=home"?>

<%@ page contentType="text/xml" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>



<gen:document 
      title="Home Index"
      last_modified="10/02/2006"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Hello! </h3>

<jsp:useBean id="v_data" class="java.util.HashMap" scope="request" />

<c:set target="${v_data}" property="s_test">
<p> Can you see this ? </p>
</c:set>

<p>
Welcome to the Littleware help center.
The help center is still under construction.
</p>
<c:out value="${v_data.s_test}" escapeXml='false' />

</gen:document>

