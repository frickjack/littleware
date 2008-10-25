<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:task="http://www.littleware.com/xml/taglib/2007/task" 
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<xsl:template match="task:tasklist">
 <b> Tasklist </b>
 <ul>
    <xsl:apply-templates />
 </ul>
</xsl:template>

<xsl:template match="task:task">
  <li> <xsl:copy-of select="./task:summary" />
     <xsl:variable name="nodename">nodexpand<xsl:number level="any" /></xsl:variable>
     <a href="javascript:lwUtil_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> 
     <span id="{$nodename}" style="display:none">
        <dl>
            <dt><b>status</b></dt>
                <dd><xsl:value-of select="./task:status" /></dd>
            <dt><b>assigned to</b></dt>
                <dd><xsl:value-of select="./task:assignto" /></dd>
            <dt><b>description</b></dt>
                <dd><xsl:copy-of select="./task:description" /></dd>
         </dl>
     </span>
   </li>
</xsl:template>


</xsl:stylesheet>
