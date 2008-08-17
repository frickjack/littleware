<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:asset="http://www.littleware.com/xml/namespace/2006/asset"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">


<xsl:template match="asset:core">
    <p class="assetdef"> 
        Asset named <b><xsl:value-of select="./asset:name" /></b> of type
        <xsl:value-of select="./asset:asset_type" />.
    </p>
    <xsl:apply-templates select="./asset:comment" />
    <table class="assetdef">
       <xsl:apply-templates select="./asset:object_id" />
       <xsl:apply-templates select="./asset:home_id" />
       <xsl:apply-templates select="./asset:to_id" />
       <xsl:apply-templates select="./asset:from_id" />
       <xsl:apply-templates select="./asset:acl_id" />
       <xsl:apply-templates select="./asset:creator_id" />
       <xsl:apply-templates select="./asset:create_date" />
       <xsl:apply-templates select="./asset:updater_id" />
       <xsl:apply-templates select="./asset:update_date" />
       <xsl:apply-templates select="./asset:start_date" />
       <xsl:apply-templates select="./asset:end_date" />
    </table>
       <xsl:apply-templates select="./asset:data" />
    <hr />
</xsl:template>

<xsl:template match="asset:comment">
      <p> 
            <b> <xsl:value-of select="local-name()" />: </b>  
           <xsl:value-of select ="." /> 
      </p>
</xsl:template>

<xsl:template match="asset:data">
      <p> 
            <b> <xsl:value-of select="local-name()" />: </b>  
           <xsl:apply-templates />
      </p>
</xsl:template>


<xsl:template match="asset:link">
    <xsl:variable name="url">/littleware/en/toolbox/view_asset.jsp?object_id=<xsl:value-of select="./@aref" /></xsl:variable>
    <a href="{$url}"><xsl:value-of select="." /></a>
</xsl:template>

<xsl:template match="asset:core/*" priority="-10">
    <xsl:choose>
    <xsl:when  test="substring(local-name(),string-length(local-name())-2) = '_id'">
        <xsl:variable name="url">/littleware/en/toolbox/view_asset.jsp?object_id=<xsl:value-of select="." /></xsl:variable>
        <tr><td> <b> <xsl:value-of select="local-name()" /> </b></td>
            <td><b>:</b></td>
            <td><a href="{$url}"><xsl:value-of select="." /></a></td>
            </tr>
    </xsl:when>
    <xsl:otherwise>
        <tr><td> <b> <xsl:value-of select="local-name()" /> </b></td>
            <td><b>:</b></td>
            <td> <p> 
               <xsl:value-of select ="." /> 
              </p>
            </td>
         </tr>
   </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
