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


<xsl:variable name="lw_doc_corner_image"><xsl:value-of select="concat($lw_doc_root, '/img/uml.jpg' )" /></xsl:variable>

<xsl:variable name="lw_doc_title">
      <xsl:value-of select="/gen:view/gen:document/@title" /> 
</xsl:variable>

<xsl:variable name="lw_doc_css">
      <link href="{$lw_doc_root}/css/global.css" rel="stylesheet" type="text/css" media="screen,print" />
      <link href="/littleware/lib/css/littleware_defaults.css" rel="stylesheet" type="text/css" media="screen,print" />
</xsl:variable>


<xsl:variable name="lw_doc_footer">
     <xsl:copy-of select="$lw_default_footer" />
</xsl:variable>

</f:view>

</xsl:stylesheet>
