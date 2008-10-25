<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:biblio="http://www.littleware.com/xml/namespace/2006/bibliography"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<xsl:template match="biblio:entry">
      <xsl:apply-templates select="./biblio:authorlist" /><xsl:text> </xsl:text><i> <xsl:value-of select="./biblio:title" /></i>,<xsl:text> </xsl:text>
      <xsl:value-of select="./biblio:publication" /><xsl:if test="./gen:date">,<xsl:text> </xsl:text><xsl:apply-templates select="./gen:date" /></xsl:if><xsl:if test="./addr:snailmail/@city">, <xsl:text> </xsl:text><xsl:value-of select="addr:snailmail/@city" />,<xsl:text> </xsl:text><xsl:value-of select="addr:snailmail/@state" /></xsl:if>
</xsl:template>

<xsl:template match="biblio:entry/gen:date">
    <xsl:choose>
      <xsl:when test="@month=1"> Jan. </xsl:when>
      <xsl:when test="@month=2"> Feb. </xsl:when>
      <xsl:when test="@month=3"> Mar. </xsl:when>
      <xsl:when test="@month=4"> Apr. </xsl:when>
      <xsl:when test="@month=5"> May </xsl:when>
      <xsl:when test="@month=6"> June </xsl:when>
      <xsl:when test="@month=7"> July </xsl:when>
      <xsl:when test="@month=8"> Aug. </xsl:when>
      <xsl:when test="@month=9"> Sep. </xsl:when>
      <xsl:when test="@month=10"> Oct. </xsl:when>
      <xsl:when test="@month=11"> Nov. </xsl:when>
      <xsl:when test="@month=12"> Dec. </xsl:when>
    </xsl:choose>
    <xsl:value-of select="@year" />
</xsl:template>

<xsl:template match="biblio:authorlist">
  <xsl:for-each select="addr:name">
     <xsl:value-of select="substring(@first, 1, 1)" />. <xsl:text> </xsl:text> <xsl:value-of select="@last" />,
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
