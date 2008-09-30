<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.0"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="../../home.xsl"?>]]>
<jsp:text>

</jsp:text>

  <jsp:directive.page contentType="text/xml;charset=UTF-8" />
  <!-- These pages are c:imported into others -->
  <jsp:output omit-xml-declaration="no" />

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
    <link rel="alternate" href="/littleware/en/home/blogs/pasquini/home.jsf"/>
    <summary>Master page for /en/home/blogs/pasquini area.  
             Just a document.
             References the 
                /en/home/blogs/pasquini/atom_feed.jsp
             feed.
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
       <c:param name="topmenu_select" value="home" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_home.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="blogs" />
  </c:import>
<link rel="alternate" type="application/atom+xml" title="pasquini feed" 
    href="/littleware/en/home/blogs/pasquini/atom_feed.jsp"
    />
</gen:websupport>
<gen:document 
      title="Home Index"
      last_modified="09/29/2006"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Hello! </h3>

<p>
Welcome to Reuben&apos;s BLOG.
We just have a set of XML blog entry files here,
that we publish by adding to the index here. 
Hopefully we&apos;ll have a more sophisticated system
supporting reader comments and RSS in the near future.
</p>

 <lw:filter begin="&lt;feed" end="&lt;/feed>">
  <c:import url="atom_feed.jsp" charEncoding="UTF-8" />
 </lw:filter>


</gen:document>
</gen:view>
</jsp:root>
