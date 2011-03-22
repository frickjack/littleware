<?xml version="1.0" encoding="UTF-8" ?>


<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:html="http://www.w3.org/1999/xhtml"
     >
    <xsl:output method="html" indent="yes"/>
<!--
http://content.lib.auburn.edu/cgi-bin/oai.exe?verb=ListRecords&set=ebsledge&metadataPrefix=oai_dc
-->

  <!-- pass-through rule -->
    <xsl:template match="@*|node()">
      <!--
     <xsl:copy>
       <xsl:apply-templates select="@*|node()"/>
     </xsl:copy>
        -->
    </xsl:template>


    <xsl:template match="/">
        <html>
            <head><title>Verify Result</title></head>
            <body>
                <h3>Verification Result: <xsl:apply-templates select="*" /></h3>
            </body>
        </html>
     </xsl:template>

     <xsl:template match="verify">
         <xsl:value-of select="." />
     </xsl:template>
</xsl:stylesheet>
