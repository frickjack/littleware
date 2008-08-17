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
    <title> /en/toolbox/copyright/home.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/home/home.jsp"/>
    <summary>Master page for /toolbox/copyright area.  
             Just a document.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com </rights>
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
       <c:param name="topmenu_select" value="toolbox" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="copyright" />
  </c:import>
</gen:websupport>
<gen:document 
      title="Copyright"
      last_modified="01/23/2007"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

    littleware asset tracker <br />
    Copyright (C) 2007 Reuben Pasquini <br />

<p>
    The littleware libraries and web content is licensed
    as free software; you can redistribute it and/or
    modify it under the terms of the 
    <a href="./lgpl.txt"> GNU Lesser General Public
    License version 2.1 </a> as published by the Free Software Foundation.
</p>
<p>
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
</p>

</gen:document>
</gen:view>
