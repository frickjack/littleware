<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" %>
<%@ page import="littleware.web.beans.BrowserType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
       xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:asset="http://www.littleware.com/xml/namespace/2006/asset"
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns:biblio="http://www.littleware.com/xml/namespace/2006/bibliography"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:quota="http://www.littleware.com/xml/namespace/2006/quota"
      xmlns:task="http://www.littleware.com/xml/taglib/2007/task" 
      xmlns:vita="http://www.littleware.com/xml/namespace/2006/vita"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns:svg="http://www.w3.org/2000/svg"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0"
     >


<!-- 
   Set the output properties 
   * SAFARI needs it html for applets to work
   * Firefox needs it xml for css to work after XSLT
   * New version of Mac WebKit fixes the Safari applet thing
      in xml output-mode, and REQUIRES xml output-mode for SVG
 -->
<%
BrowserType n_browser = BrowserType.getBrowserFromAgent (
                  request.getHeader( BrowserType.OS_USER_AGENT )
            );
request.setAttribute ( "s_agent", n_browser );
//<c:out value="${s_agent}" />
%>


<xsl:param name="html-content-type" />

<xsl:template match="xhtml:*">
 <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

<xsl:template match="svg:*">
 <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

<xsl:template match="@*">
 <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
 </xsl:copy>
</xsl:template>

<!-- do not propagate xsl-stylesheet, or risk infinite loop with wildcard match -->
<xsl:template match="processing-instruction()" />

<!-- some default icons -->
<xsl:variable name="lw_icon_email" select="concat($lw_doc_root, '/icons/geronimo/mail_16.gif' )" />
<xsl:variable name="lw_icon_folder" select="concat($lw_doc_root, '/icons/geronimo/foldr_16.gif' )" />
<xsl:variable name="lw_icon_external_link" select="concat($lw_doc_root, '/icons/geronimo/linkext7.gif' )" />


<xsl:variable name="lw_default_footer">
     <xsl:if test="/gen:view/gen:document/@last_modified">
         <center> <i> Last modified <xsl:value-of select="/gen:view/gen:document/@last_modified" /> </i> <br /> </center>
    </xsl:if>
   <center> Copyright 2007, littleware </center> <br />
</xsl:variable>

<xsl:variable name="lw_default_root" select="'/littleware/lib'" />
<xsl:variable name="lw_default_corner_image" select="'/littleware/lib/img/pandas.jpg'" />

<!-- root rule -->
<xsl:template match="/">
 <html
      xmlns="http://www.w3.org/1999/xhtml"
    >

 <head>
    <title> <xsl:value-of select="$lw_doc_title" />   
                </title>
 
       <xsl:copy-of select="$lw_doc_css" />

   <script language="JavaScript" src="{$lw_doc_root}/js/util.js" />
 
   <xsl:apply-templates select="/gen:view/gen:websupport/xhtml:link" />

 </head>
 <body>
   <table class="layout" width="855">
   <tr class="layout" height="110">
        <td width="200"> 
          <xsl:if test="$lw_doc_corner_image != ''">
           <img width="200" height="100" src="{$lw_doc_corner_image}"/> 
          </xsl:if>
          </td>
        <td width="650" align="center"> 
             <xsl:apply-templates select="gen:view/gen:websupport/gen:topmenu" />
           </td>
            <td width="5" />
   </tr>
   <tr>
        <td class="layout" valign="top">
        <c:choose>
          <c:when test="${lw_user.guest}">
             <p class="bordermenu">
             Welcome, <b> 
                <c:out value="${lw_user.user.name}" /> </b>
             </p>
            <small> 
             <p class="bordermenu">
                If you are not
                <c:out value="${lw_user.user.name}" />,
                then please
                     <a href="/littleware/en/account/logout.jsf"> logout </a>,
                     <a href="/littleware/en/account/login.jsf"> login </a>,
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

             <xsl:apply-templates select="gen:view/gen:websupport/gen:sidemenu" />

         </td>
        <td class="layout" valign="top"  bgcolor="white" id="contentArea">
          <xsl:apply-templates select="gen:view/gen:document" />
        </td>
        <td />
    </tr>
    <tr>
       <td colspan="3" class="layout">
             <xsl:copy-of select="$lw_doc_footer" />
       </td>
    </tr>
   </table>

 </body>
 </html>

</xsl:template>

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


<xsl:variable name="lw_doc_root"><xsl:value-of select="$lw_default_root" /></xsl:variable>


<xsl:variable name="lw_doc_corner_image"><xsl:value-of select="concat($lw_doc_root, '/img/lware.jpg' )" /></xsl:variable>

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
     <a href="javascript:lw_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> <br /> 
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
     <a href="javascript:lw_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> <br /> 

     <span id="{$nodename}" style="display:none">
        <xsl:apply-templates select="./gen:description" />
     </span>
</xsl:template>

<xsl:template match="gen:date">
    <xsl:if test="./@day">
       <xsl:value-of select="./@day" />/
    </xsl:if>
    <xsl:value-of select="./@month" />/
    <xsl:value-of select="./@year" />
</xsl:template>

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

<xsl:template match="task:tasklist">
 <b> Tasklist </b>
 <ul>
    <xsl:apply-templates />
 </ul>
</xsl:template>

<xsl:template match="task:task">
  <li> <xsl:copy-of select="./task:summary" />
     <xsl:variable name="nodename">nodexpand<xsl:number level="any" /></xsl:variable>
     <a href="javascript:lw_toggle( '{$nodename}' )" style="font-size:80%" >[Details &#187;]</a> 
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


<xsl:template match="/vita:vita">
   <xsl:apply-templates />
</xsl:template>

<xsl:template match="vita:vita/addr:contact_info" priority="1">

   <center><b> <xsl:value-of select="./addr:name/@first" /> 
               <xsl:text> </xsl:text>
               <xsl:value-of select="./addr:name/@last" />
                 </b><br />
             <xsl:apply-templates select="./addr:email" /> <br />
             <xsl:apply-templates select="./addr:snailmail" /> <br />
      </center>

   <hr />

</xsl:template>

<xsl:template match="vita:work_experience">
 <h3> Work Experience </h3>
     <xsl:apply-templates />
   <hr />
</xsl:template>

<xsl:template match="vita:education">
 <h3> Education </h3>
     <xsl:apply-templates />
   <hr />
</xsl:template>

<xsl:template match="vita:skillset">
 <h3> Skillset </h3>
   <ul>
     <xsl:apply-templates />
   </ul>
   <hr />
</xsl:template>

<xsl:template match="vita:skill">
     <p>
    <b><xsl:value-of select="./@name" /></b>:
       <xsl:value-of select="." />
     </p>
</xsl:template>


<xsl:template match="vita:job">

    <h3> <xsl:value-of select="./@start" />-<xsl:value-of select="./@end" />
         : <xsl:value-of select="./@title" />, 
           <xsl:value-of select="./vita:employer/@name" />
       </h3>

       <xsl:if test="vita:employer/gen:description">
        <p> <i> Employer </i> </p>
          <xsl:apply-templates select="vita:employer" />
       </xsl:if>
        <p> <i> Job Description </i> </p>
          <xsl:apply-templates select="gen:description" />

  </xsl:template>


<xsl:template match="vita:reference">
     <p> Reference: <a mailto="{./addr:email}"> <xsl:value-of select="./@name" /> </a>, <xsl:value-of select="./@connection" />
       </p>
</xsl:template>

<xsl:template match="vita:degree">
    <h3> <xsl:value-of select="./@name" /> in 
               <xsl:value-of select="./@field" />,
          <xsl:value-of select="./@end" />,
         <xsl:apply-templates select="./vita:school/addr_contact_info/gen:url" />
         <a href="{./vita:school/addr:contact_info/gen:url}"> <xsl:value-of select="./vita:school/@name" /> 
            <img src="{$lw_icon_external_link}" />
         </a> 
       </h3>
      <xsl:apply-templates />
</xsl:template>

<xsl:template match="vita:publist">
     <p> <i> Publications </i> </p>
     <ul>
      <xsl:for-each select="biblio:entry">
         <li>
          <xsl:apply-templates select="." />
         </li>
      </xsl:for-each>
     </ul>
</xsl:template>

</xsl:stylesheet>
