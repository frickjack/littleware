<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.littleware.com/xml/taglib/2006/general" 
               prefix="lw" %>  

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
    <title> /en/toolbox/src/home.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/home/home.jsp"/>
    <summary> Source code info and download.
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
       <c:param name="topmenu_select" value="toolbox" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="src" />
  </c:import>
</gen:websupport>

<gen:document 
      title="SRC Home"
      last_modified="02/06/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Littleware Open Source Download </h3>

<p>
Littleware&apos;s codebase relies upon several open
source projects including 
<a href="http://www.java.com">java</a>,
<a href="http://www.postgresql.org"> postgres </a>,
<a href="http://tomcat.apache.org"> tomcat </a>,
<a href="http://www.apache.org"> apache </a>,
<a href="http://db.apache.org/derby"> derby </a>,
<a href="http://jakarta.apache.org/bsf/"> bean scripting framework (bsf) </a>,
and many others.
With gratitude to the developers that maintain these
projects, we have released the littleware
codebase under the <a href="./lgpl.txt">Lesser GPL</a> 
copyright license.
</p>
<p>
We currently only have two packages available for download,
and both these packages are in a <i>beta</i> state.
We are working our way through a 
constatly growing <a href="todo.jsp">TODO</a>
list.  <a href="mailto:${lw_defaults.defaults['contact_email']}">Let us know</a>
if you would like to contribute, or just send us a patch.
<dl>
<dt><b>Source 
    <a href="/littleware/lib/downloads/littleware_dist.zip">download</a>
    </b></dt>
    <dd>
    The littleware source code includes the following.
      <ul>
      <li> DDL scheam definition for the postgres and derby databases,
            and a derby database template.
            </li>
      <li> Java source code and tld definitions. </li>
      <li> Versions of several third-party jars required to
             build the littleware jars and run the JUnit test cases.
            </li>
      <li> An <a href="http://www.apple.com/macosx/features/xcode/">XCode IDE</a>
            project configured for developing and building the java
            codebase.  
            </li>
      <li> An <a href="http://ant.apache.org"> ANT</a> build script
           that includes rules to build the littleware jars, 
           and run the regression tests. 
          </li>
      <li> The JSPs and support documents (CSS, javascript, etc.)
             that make up this web site. </li>
      <li> A small library of python scripts </li>
      <li> Configuration files for supporting services like Mac launchd </li>
      </ul>
     We will be writing documentation and blogs describing how to work
     with the littleware codebase in the near future.
    </dd>
    <br />
    <!--
<dt> <b>littlego client utilities
      <a href="/littleware/lib/downloads/littlego.zip">download </a>
     </b></dt>
     <dd>
      The littlego installation is a simple Library/* directory hierarchy
      that includes a build of the littleware.jar client library
      and its dependencies, and several useful command-line tools.
      This <a href="/littleware/en/home/blogs/littleware_news/2007/release_v1_3_20070828.jsp"> blog </a> entry has more details on the latest release.
     </dd>
       -->
</dl>

</p>

       
</gen:document>

</gen:view>
