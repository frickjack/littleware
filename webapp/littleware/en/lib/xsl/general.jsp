<?xml version="1.0" encoding="UTF-8"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
 >
  <jsp:directive.page contentType="text/xml;charset=UTF-8" />
  <jsp:output omit-xml-declaration="false" />

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

<jsp:scriptlet>
/** <![CDATA[
  <jdoc:docinfo>
    <jdoc:description> 
         Littleware English XHTML stylesheet for the gen: general XML tags.
    </jdoc:description>
  </jdoc:docinfo>
]]>*/
</jsp:scriptlet>



<xsl:template match="xhtml:a">
    <xsl:copy-of select="." />
    <xsl:choose>
      <xsl:when test="@type='external'">
        <img src="{$lw_icon_external_link}" />
      </xsl:when>
      <xsl:when test="starts-with(@href, 'http' ) ">
        <img src="{$lw_icon_external_link}" />
      </xsl:when>
      <xsl:when test="starts-with(@href, 'mailto:' ) ">
        <img src="{$lw_icon_email}" />
      </xsl:when>
    </xsl:choose>
</xsl:template>


<xsl:template match="xhtml:applet">
  <object 
          id="{./@id}"
          classid="clsid:CAFEEFAC-0015-0000-0000-ABCDEFFEDCBA"
          width="{./@width}" height="{./@height}" 
          codebase="http://java.sun.com/update/1.6.0/jinstall-6-windows-i586.cab#Version=1,5,0,0"
       >
    <param name="code" value="{./@code}" />
    <param name="codebase" value="/littleware/lib/jar" />
    <param name="archive" value="littleware.jar,java-getopt.jar,mailapi.jar,mail.jar" />
    <param name="type" value="application/x-java-applet;version=1.5" />
    <param name="scriptable" value="true" />
    <xsl:apply-templates select="xhtml:param" />
    <comment>
        <embed 
           type="application/x-java-applet;version=1.5" 
           width="{./@width}" height="{./@height}" 
           code="{./@code}"
           codebase="/littleware/lib/jar" 
           pluginspage="http://java.sun.com/javase/downloads/ea.jsp"
           archive="littleware.jar,java-getopt.jar,mailapi.jar,mail.jar" 
           scriptable="true"
          >
         <xsl:for-each select="xhtml:param">
          <xsl:attribute name="{./@name}"><xsl:value-of select="./@value" /></xsl:attribute>
	 </xsl:for-each>
        </embed>

            <noembed>
                Must download <a href="http://java.com/download">Java 2 SDK</a> for APPLET support!!
            </noembed>
    </comment>
  </object> <br />
  <small>
     ( <a href="/littleware/en/toolbox/.java.policy">.java.policy info</a>)
  </small> <br />
</xsl:template>

<xsl:template match="gen:topmenu">
    <table width="100%" class="bordermenu">
       <tr class="bordermenu">
            <xsl:apply-templates />
       </tr>
    </table>
</xsl:template>

<xsl:template match="gen:sidemenu">
    <table width="100%" class="bordermenu">
       <xsl:apply-templates />
    </table>
</xsl:template>


<xsl:template match="gen:sidemenu/gen:menuitem">
    <tr class="bordermenu"> <td class="bordermenu">
    <xsl:choose>
       <xsl:when test="./@name=../@select">
         <font color="red">*</font>
         <xsl:apply-templates />
         <xsl:apply-templates select="gen:submenu/*" />
       </xsl:when>
       <xsl:otherwise>
         <xsl:apply-templates />
       </xsl:otherwise>
    </xsl:choose>
   </td></tr>
</xsl:template>

<xsl:template match="gen:submenu" />

<xsl:template match="gen:topmenu/gen:menuitem">
    <td class="bordermenu">
    <xsl:choose>
       <xsl:when test="./@name=../@select">
         <xsl:value-of select="./xhtml:a" />
       </xsl:when>
       <xsl:otherwise>
         <!-- Hack to keep things working with old Safari -->
         <a href="{./xhtml:a/@href}"><xsl:value-of select="./xhtml:a" /></a>
       </xsl:otherwise>
    </xsl:choose>
   </td>
</xsl:template>

<xsl:template match="gen:websupport" />

<xsl:template match="gen:url">
    <xsl:choose>
    <xsl:when test="@type='external'">
      <a href="{.}"> <xsl:value-of select="." /> 
            <img src="{$lw_icon_external_link}" />
         </a> 
    </xsl:when>
    <xsl:otherwise>
      <a href="{.}"> <xsl:value-of select="." /> </a>
    </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template match="gen:error">
 <hr />
  <center><font color="red"> Error </font></center>
  <xsl:apply-templates />
 <br />
 <hr />
</xsl:template>

<xsl:template match="gen:faqlist">
   <ul>
     <xsl:apply-templates select="gen:faq"/>
   </ul>
</xsl:template>

<xsl:template match="gen:faq">
   <li> 
     <small>(<xsl:value-of select="@last_updated" />)</small> <xsl:text> </xsl:text>
     <xsl:copy-of select="./gen:summary" /> <br />
     <xsl:variable name="nodename">faqnode<xsl:number level="any" /></xsl:variable>
     <a href="javascript:lwUtil_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> <br /> 
     <!-- popup garbage:
     <span id="{$nodename}" style="display:none;position:absolute;background:#fff;border:1px solid #369;margin:-.5ex 1.5ex;padding:0 0 .5ex .8ex;width:16ex;line-height:1.9;z-index:1000">
     -->

     <span id="{$nodename}" style="display:none">
        <xsl:apply-templates select="./gen:description" />
     </span>
   </li>
</xsl:template>

<xsl:template match="gen:expand">
     <xsl:apply-templates select="./gen:summary" /> 
     <xsl:variable name="nodename">nodexpand<xsl:number level="any" /></xsl:variable>
     <a href="javascript:lwUtil_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> <br /> 

     <span id="{$nodename}" style="display:none">
        <xsl:apply-templates select="./gen:description" />
     </span>
</xsl:template>

<xsl:template match="gen:path">
   <em><xsl:apply-templates /></em>
</xsl:template>

<xsl:template match="gen:date">
    <xsl:if test="./@day">
       <xsl:value-of select="./@day" />/
    </xsl:if>
    <xsl:value-of select="./@month" />/
    <xsl:value-of select="./@year" />
</xsl:template>

</xsl:stylesheet>

</jsp:root>
