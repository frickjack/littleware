<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:vita="http://www.littleware.com/xml/namespace/2006/vita"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:biblio="http://www.littleware.com/xml/namespace/2006/bibliography"
      xmlns="http://www.w3.org/1999/xhtml"
      version="1.0">

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
             <xsl:apply-templates select="./gen:url" /> 
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
