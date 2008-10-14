<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<c:choose>
<c:when test="${param.style == null}">
<?xml-stylesheet type="text/xsl" href="demo.xsl"?>
</c:when>
<c:otherwise>
<?xml-stylesheet type="text/xsl" href="${param.style}"?>
</c:otherwise>
</c:choose>

<gen:document 
    title="bla"
    last_modified="11/11/2006"
    xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.w3schools.com note.xsd"
   >

BLA BLA BLA

<applet 
        code="Demo.class"
	width="400"
	height="50">
  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</applet>

</gen:document>

