<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<f:view>
<f:loadBundle basename="littleware.web.messages.Messages" var="lw_msgs" />

<xsl:variable name="lw_doc_root"><xsl:value-of select="$lw_default_root" /></xsl:variable>


<xsl:variable name="lw_doc_corner_image"><xsl:value-of select="concat($lw_doc_root, '/img/lware.jpg' )" /></xsl:variable>

<xsl:variable name="lw_doc_title">
      <xsl:value-of select="/gen:document/@title" /> 
</xsl:variable>

<xsl:variable name="lw_doc_css">
      <link href="{$lw_doc_root}/css/global.css" rel="stylesheet" type="text/css" media="screen,print" />
      <link href="/littleware/lib/css/littleware_defaults.css" rel="stylesheet" type="text/css" media="screen,print" />
</xsl:variable>


<xsl:variable name="lw_doc_top_menu">
     <xsl:copy-of select="$lw_default_top_menu" />
</xsl:variable>

<xsl:variable name="lw_doc_side_menu">
        <c:choose>
          <c:when test="${lw_user.authenticatedName != null}">
             <p class="bordermenu">
             Welcome, <b> 
                <h:outputText id="out1" escape="false" value="#{lw_user.authenticatedName}" /> </b>
             </p>
            <small> 
             <p class="bordermenu">
                If you are not
                <h:outputText id="out2" escape="false" value="#{lw_user.authenticatedName}" />,
                then please
                     <a href="/littleware/en/account/login.jsf"> login </a>
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

    <table width="100%" class="bordermenu">
    <tr class="bordermenu"><td class="bordermenu">
        <a href="/littleware/en/home/home.jsf" title="Home"> 
          <img src="{$lw_icon_folder}" />
          Home
         </a> 
        </td></tr>
    <tr class="bordermenu"><td class="bordermenu">
        <a href="/littleware/en/home/faq.jsf" title="FAQ"> 
          <img src="{$lw_icon_folder}" />
           FAQ </a> 
        </td></tr>
    <tr class="bordermenu"><td class="bordermenu">
        <a href="/littleware/en/toolbox/home.jsf" title="Product Overview"> 
          <img src="{$lw_icon_folder}" />
           Products </a> 
        </td></tr>
    <tr class="bordermenu"><td class="bordermenu">
        <a href="/littleware/en/home/addressbook.jsf"> 
          <img src="{$lw_icon_folder}" />
           Contact Us </a> 
        </td></tr>
   </table>
</xsl:variable>

<xsl:variable name="lw_doc_footer">
     <center> <i> Last modified <xsl:value-of select="/gen:document/@last_modified" /> </i> <br /> </center>
     <xsl:copy-of select="$lw_default_footer" />
</xsl:variable>

</f:view>

</xsl:stylesheet>
