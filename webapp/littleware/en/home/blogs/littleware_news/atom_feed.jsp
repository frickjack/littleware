<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="application/atom+xml" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://www.littleware.com/xml/taglib/2006/general" 
               prefix="lw" %>  

<%--

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

--%>

<feed 
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:x="http://java.sun.com/jsp/jstl/xml"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns="http://www.w3.org/2005/Atom"
      >
<!--
   Atom-feed generate - sucks atom:entry data out of
   list of referenced URL's with the atom:entry embedded.
-->

  <title>Littleware News Feed</title> 
  <link href="http://littleware.com/littleware/"/>
  <author> 
    <name>Reuben Pasquini</name>
  </author> 

  <jsp:scriptlet>
        //<![CDATA[
        // Replace this hard-code with call out to littleware 
        // once we get the blog-data moved behind the API
        String[] v_entry = { 
             "/en/home/blogs/littleware_news/2007/release_v1_4_20070925.jsp",
             "/en/home/blogs/littleware_news/2007/release_v1_3_20070828.jsp",
             "/en/home/blogs/littleware_news/2007/release_v1_2_20070811.jsp",
             "/en/home/blogs/littleware_news/2007/xml_javascript_20070808.jsp",
             "/en/home/blogs/littleware_news/2007/mvc_20070808.jsp",
             "/en/home/blogs/littleware_news/2007/deployment_model_20070213.jsp",
             "/en/home/blogs/littleware_news/2007/macfaq.jsp",
             "/en/home/blogs/littleware_news/2007/finally_online_20070128.jsp" 
         };
        pageContext.setAttribute ( "v_entry", v_entry );
        //]]>
  </jsp:scriptlet>

  <lw:cache id="cache_littleware_news" ageoutSecs="30">
          <c:set var="s_latest_entry_xml">
              <lw:filter begin="<entry" end="</entry>">
                  <c:import url="${v_entry[0]}" />
              </lw:filter>
          </c:set>

          <x:parse var="xml_latest" xml="${s_latest_entry_xml}" />
        
          <!-- Namespace handling in x:out is fricked up -->
          <id><x:out select="$xml_latest//*[local-name()='id']" /></id>
          <updated><x:out select="$xml_latest//*[local-name()='updated']" /></updated>
        
        
          <c:out value="${s_latest_entry_xml}" escapeXml="false" />
        
          <c:forEach var="s_entry_url" items="${v_entry}" begin="1">
              <lw:filter begin="<entry" end="</entry>">
                  <c:import url="${s_entry_url}" />
              </lw:filter>
          </c:forEach>
  </lw:cache>

</feed>
