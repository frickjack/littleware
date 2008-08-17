<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>
<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom documentation entry -->
  <entry>
    <title> /en/account/welcome.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/account/welcome.jsp"/>
    <summary>Welcome page upon successful registration
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

  <jsp:directive.page 
          contentType="text/xml;charset=UTF-8" 
      />
  <jsp:output omit-xml-declaration="false" />
  <jsp:text>

  </jsp:text>

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
  <c:import url="/en/lib/jspf/sidemenu_account.jspf" charEncoding="UTF-8" />
</gen:websupport>



<gen:document 
      title="Home Index"
      last_modified="11/27/2006"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns="http://www.w3.org/1999/xhtml"
     >
<f:view>

<h3> Thank you for registering!! </h3>

<p>
Your new littleware account password has been e-mailed to you
at <h:outputText value="#{lw_register.email}" />.
</p>

</f:view>
</gen:document>

</gen:view>

</jsp:root>

