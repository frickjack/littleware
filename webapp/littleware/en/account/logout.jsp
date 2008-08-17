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
    <title> /en/account/logout.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/account/logout.jsp"/>
    <summary>Simple logout page - invalidates session, and says thankyou.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

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
      title="Login"
      last_modified="02/07/2007"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<jsp:scriptlet>
    session.invalidate ();
</jsp:scriptlet>

<h3> Logout Ok </h3>

<p>
Have a good day!
</p>

</gen:document>

</gen:view>

</jsp:root>
