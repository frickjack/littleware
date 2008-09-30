<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
 >
  <jsp:directive.page contentType="text/xml;charset=UTF-8" />
  <jsp:output omit-xml-declaration="false" />

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

  <!--
     Littleware English XHTML stylesheet for atom-feed tags.
  -->

<xsl:template match="atom:feed">
    <h3> <xsl:value-of select="./atom:title" /> </h3>
    <dl>
    <xsl:apply-templates select="./atom:entry" />
    </dl>
</xsl:template>

<xsl:template match="atom:entry">
    <dt> <b> <xsl:value-of select="./atom:title" /> </b>,
          updated by <xsl:value-of select="./atom:author/atom:name" />
          on <xsl:value-of select="./atom:updated" />
         </dt>
    <dd>
       <xsl:apply-templates select="./atom:summary" />
       <a href="{./atom:link/@href}"> Read more ... </a>
    </dd>
</xsl:template>


</xsl:stylesheet>

</jsp:root>
