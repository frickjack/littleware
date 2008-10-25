<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:asset="http://www.littleware.com/xml/namespace/2006/asset"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<xsl:template match="addr:contact_info">

   <xsl:apply-templates select="./addr:email" />

</xsl:template>

<!-- come up with an e-mail icon, move out to addressbook.xsl -->
<xsl:template match="addr:email">
    <a href="mailto:{.}"> 
         <img src="{$lw_icon_email}" /> 
         <xsl:value-of select="." /> 
    </a>
</xsl:template>

<xsl:template match="addr:snailmail">
  <xsl:value-of select="text()" />, 
  <xsl:value-of select="./@city" />, <xsl:value-of select="./@state" />
      <xsl:text> </xsl:text> <xsl:value-of select="./@zip" /> <br />
  <xsl:apply-templates select="./addr:phone" />
</xsl:template>

<xsl:template match="asset:core/asset:data/addr:contact">
     <b> Name: </b> 
        <xsl:value-of select="./addr:first" />
      <xsl:text> </xsl:text> 
        <xsl:value-of select="../../asset:name" />
     <br />
</xsl:template>

<xsl:template match="asset:core/asset:data/addr:addr_info">
<b> address: </b> <br />
<table>
<tr> <td> type </td> <td>:</td><td> <xsl:value-of select="./addr:label" /></td> </tr>
<tr> <td> e-mail </td> <td>:</td><td> <xsl:apply-templates select="./addr:email" /></td> </tr>
</table>
</xsl:template>

</xsl:stylesheet>
