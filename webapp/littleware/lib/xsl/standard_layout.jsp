<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" %>
<%@ page import="littleware.web.beans.BrowserType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0"
     >

<!-- 
   Set the output properties 
   * SAFARI needs it html for applets to work
   * Firefox needs it xml for css to work after XSLT
   * New version of Mac WebKit fixes the Safari applet thing
      in xml output-mode, and REQUIRES xml output-mode for SVG
 -->
<%
BrowserType n_browser = BrowserType.getBrowserFromAgent (
                  request.getHeader( BrowserType.OS_USER_AGENT )
            );
request.setAttribute ( "s_agent", n_browser );
//<c:out value="${s_agent}" />
%>


<c:choose>
    <c:when test="${s_agent == 'WEBKIT41x'}">
      <xsl:output method="html" encoding="UTF-8" omit-xml-declaration="no" />
    </c:when>
    <c:otherwise>
      <xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" />
    </c:otherwise>
</c:choose>

<xsl:param name="html-content-type" />

<xsl:template match="@*|node()">
 <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

<!-- do not propagate xsl-stylesheet, or risk infinite loop with wildcard match -->
<xsl:template match="processing-instruction()" />

<!-- some default icons -->
<xsl:variable name="lw_icon_email" select="concat($lw_doc_root, '/icons/geronimo/mail_16.gif' )" />
<xsl:variable name="lw_icon_folder" select="concat($lw_doc_root, '/icons/geronimo/foldr_16.gif' )" />
<xsl:variable name="lw_icon_external_link" select="concat($lw_doc_root, '/icons/geronimo/linkext7.gif' )" />


<xsl:variable name="lw_default_footer">
     <xsl:if test="/gen:view/gen:document/@last_modified">
         <center> <i> Last modified <xsl:value-of select="/gen:view/gen:document/@last_modified" /> </i> <br /> </center>
    </xsl:if>
   <center> Copyright 2007, littleware </center> <br />
</xsl:variable>

<xsl:variable name="lw_default_root" select="'/littleware/lib'" />
<xsl:variable name="lw_default_corner_image" select="'/littleware/lib/img/pandas.jpg'" />

<!-- root rule -->
<xsl:template match="/">
 <html
      xmlns="http://www.w3.org/1999/xhtml"
    >

 <head>
    <title> <xsl:value-of select="$lw_doc_title" />   
                </title>
 
       <xsl:copy-of select="$lw_doc_css" />

   <!-- IE freaks out if we just do a <script /> instead of <script></script> -->
   <script language="JavaScript" src="{$lw_doc_root}/js/util.js"></script>
 
   <xsl:apply-templates select="/gen:view/gen:websupport/xhtml:link" />

 </head>
 <body>
   <table class="layout" width="855">
   <tr class="layout" height="110">
        <td width="200"> 
          <xsl:if test="$lw_doc_corner_image != ''">
           <img width="200" height="100" src="{$lw_doc_corner_image}"/> 
          </xsl:if>
          </td>
        <td width="650" align="center"> 
             <xsl:apply-templates select="gen:view/gen:websupport/gen:topmenu" />
           </td>
            <td width="5" />
   </tr>
   <tr>
        <td class="layout" valign="top">
        <c:choose>
          <c:when test="${lw_user.authenticatedName != null}">
             <p class="bordermenu">
             Welcome, <b> 
                <c:out value="${lw_user.authenticatedName}" /> </b>
             </p>
            <small> 
             <p class="bordermenu">
                If you are not
                <c:out value="${lw_user.authenticatedName}" />,
                then please
                     <a href="/littleware/en/account/logout.jsf"> logout </a>,
                     <a href="/littleware/en/account/login.jsf"> login </a>,
                       or 
                     <a href="/littleware/en/account/register.jsf"> register </a>.
             </p>
             </small>
           </c:when>
           <c:otherwise>
               <p class="bordermenu"> 
                    Welcome to Littleware.
                     Please 
                     <a href="/littleware/en/account/login.jsf"> login </a>
                       or 
                     <a href="/littleware/en/account/register.jsf"> register </a>.
                  </p>
            </c:otherwise>
           </c:choose>

             <xsl:apply-templates select="gen:view/gen:websupport/gen:sidemenu" />

         </td>
        <td class="layout" valign="top"  bgcolor="white" id="contentArea">
          <xsl:apply-templates select="gen:view/gen:document" />
        </td>
        <td />
    </tr>
    <tr>
       <td colspan="3" class="layout">
             <xsl:copy-of select="$lw_doc_footer" />
       </td>
    </tr>
   </table>

 </body>
 </html>

</xsl:template>

</xsl:stylesheet>


