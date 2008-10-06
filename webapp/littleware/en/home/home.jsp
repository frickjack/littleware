<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>

  <jsp:directive.page 
          contentType="text/xml;charset=UTF-8" 
          errorPage="/en/home/master_error.jsp"
      />
  <jsp:output omit-xml-declaration="false" />

<jsp:useBean id="lw_defaults" class="littleware.web.beans.DefaultsBean" />

<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom documentation entry -->
  <entry>
    <title> /en/home/home.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/home/home.jsp"/>
    <summary>Master page for /home area.  
             Just a document.
             References the 
                /en/home/blogs/littleware_news/atom_feed.jsp
             feed.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

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
       <c:param name="sidemenu_select" value="home" />
  </c:import>

<link rel="alternate" type="application/atom+xml" title="littleware feed" 
    href="/littleware/en/home/blogs/littleware_news/atom_feed.jsp"
    />
</gen:websupport>

<gen:document 
      title="Littleware Home"
      last_modified="09/23/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >
<h3> <img src="/littleware/lib/icons/geronimo/home_16.gif" /> LittleHome </h3>

<div>
<p>
Welcome to Littleware&apos;s home on the web.
We're still under active development, but
you will find information about Littleware
<a href="/littleware/toolbox/home.jsf">products and services</a>,
<a href="/littleware/helpdesk/home.jsf">customer support</a>,
developer <a href="blogs/home.jsf">blogs</a>,
and <a href="/littleware/toolbox/doc/home.jsf"> API documentation </a>.
</p>
<p>
Littleware is a sole proprietorship 
developing an extensible enterprise management system,
and providing software and technology support services.
We currently offer 
 <a href="/littleware/en/toolbox/home.jsf"> consulting services</a> 
to clients seeking support for a software development
project or setting up and managing a workgroup computer network.
We will soon offer our first few
<a href="/littleware/en/toolbox/home.jsf"> software services </a>
for subscription access on our servers.
</p>
<p>
<a href="mailto:${lw_defaults.defaults['contact_email']}">Let us know</a>
about any problems you come across.
</p>
</div>
<div>
	
 <c:catch var="frick">
 <lw:filter begin="&lt;feed" end="&lt;/feed>">
  <c:import url="blogs/littleware_news/atom_feed.jsp" charEncoding="UTF-8" />
 </lw:filter>
 </c:catch>

 <c:out value="${frick}" />
 

</div>

</gen:document>

</gen:view>

</jsp:root>

