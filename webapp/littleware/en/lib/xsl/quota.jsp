<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<xsl:template match="quota:quotaset">
<p> <b> Quota </b>
  <table border="0">
    <tr>
        <th><b> type </b></th><th><b>:</b></th><th><b> limit </b></th>
     </tr>
    <xsl:apply-templates />
  </table>
</p>
</xsl:template>

<xsl:template match="quota:quotaspec">
    <tr>
       <td> <xsl:value-of select="./@type" /> </td>
       <td> : </td>
       <td> <xsl:value-of select="./@limit" /> </td>
    </tr>
</xsl:template>

</xsl:stylesheet>
