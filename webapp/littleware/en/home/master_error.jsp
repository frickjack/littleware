<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:fn="http://java.sun.com/jsp/jstl/functions"
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>
<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom entry -->
  <entry>
    <title> /en/home/master_error.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/home/master_error.jsp"/>
    <summary>Default error page for the application-server
            to route clients to on jsp processing failure.
            Just a document.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

  <jsp:directive.page 
           contentType="text/xml;charset=UTF-8" 
           isErrorPage="true"
       />
  <jsp:output omit-xml-declaration="false" />

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="home" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_home.jspf" charEncoding="UTF-8" />

</gen:websupport>

<gen:document 
      title="Error"
      last_modified="02/01/2007"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >
<!-- switch this over to a CSS style -->
<p>
 <font color="red"><b>Error processing page ...</b></font>
</p>

<c:set var="s_message">
<jsp:expression>
    (exception == null) ? null : exception.getMessage ()
</jsp:expression>
</c:set>

<c:choose>
<c:when test="${fn:startsWith( s_message,'SessionExpired')}">
  <p>
  Your session expired.  
  Please <a href="/littleware/en/account/login.jsf">login</a>
  to access the resource you requested.
  </p>
</c:when>
<c:otherwise>
  <p>
  You have been routed here, 
  because there was an unexpected error processing your request.
  We apologize for the invonvenience.
  </p>
</c:otherwise>
</c:choose>

<gen:expand>
   <gen:summary>Exception information</gen:summary>
   <gen:description>
   <pre>
   <c:set var="s_message">
      <jsp:expression> 
        littleware.base.BaseException.getStackTrace ( exception ) 
      </jsp:expression>
   </c:set>
   <c:out value="${s_message}" escapeXml="true" />
   </pre>
   </gen:description>
</gen:expand>

</gen:document>

</gen:view>

</jsp:root>

