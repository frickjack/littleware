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
  <!-- Atom entry -->
  <entry>
    <title> /en/home/home.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/home/blogs/littleware_news/home.jsf"/>
    <summary>Master page for /en/home/blogs/littleware_news area.  
             Just a document.
             References the 
                /en/home/blogs/littleware_news/atom_feed.jsp
             feed.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

  <jsp:directive.page 
          contentType="text/xml;charset=UTF-8" 
          errorPage="/en/home/master_error.jsp"
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
       <c:param name="topmenu_select" value="home" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_home.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="blogs" />
       <c:param name="submenu_select" value="littleware_news" />
  </c:import>
<link rel="alternate" type="application/atom+xml" title="littleware feed" 
    href="/littleware/en/home/blogs/littleware_news/atom_feed.jsp"
    />
</gen:websupport>

<gen:document 
      title="Home Index"
      last_modified="02/01/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

 <lw:filter begin="&lt;feed" end="&lt;/feed>">
  <c:import url="atom_feed.jsp" charEncoding="UTF-8" />
 </lw:filter>

</gen:document>

</gen:view>

</jsp:root>
