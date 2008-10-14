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
--%>


<feed 
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:x="http://java.sun.com/jsp/jstl/xml"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/2005/Atom"
      >

  <title>Pasquini News Feed</title> 
  <link href="http://littleware.com/littleware/"/>
  <author> 
    <name>Reuben Pasquini</name>
  </author> 

  <jsp:scriptlet>
        //<![CDATA[
        // Replace this hard-code with call out to littleware 
        // once we get the blog-data moved behind the API
        String[] v_entry = { "/en/home/blogs/pasquini/2007/dateline_sucks_20061027.jsp" };
        pageContext.setAttribute ( "v_entry", v_entry );
        //]]>
  </jsp:scriptlet>

  <lw:cache id="cache_pasquini_news" ageoutSecs="30">
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
