<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">



<xsl:template match="di:diary">

    <h3> bLog for <xsl:value-of select="./@owner" /> </h3>

<hr />

   <xsl:apply-templates/>
</xsl:template>

<xsl:template match="di:section">
    <b> <xsl:value-of select="./@topic" /> </b> 
    <div>
    <xsl:apply-templates />
    </div>
</xsl:template>

<xsl:template match="di:journal">
    <xsl:apply-templates />
    <hr />
</xsl:template>


<xsl:template match="di:entry">
Entry 
     <xsl:value-of select="./di:timestamp/@year"/>/
     <xsl:value-of select="./di:timestamp/@month"/>/
     <xsl:value-of select="./di:timestamp/@day"/> 
     at <xsl:value-of select="./di:location/@city"/>:
      <b><xsl:value-of select="./@title" /></b> <br />

   <xsl:apply-templates />
</xsl:template>


</xsl:stylesheet>
