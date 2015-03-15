# Introduction #

The [Voyager-to-Vufind tool](http://ivy2maven2.littleware.googlecode.com/hg/webstart/vygr2vfnd.jnlp) (_v2v_) can not only copy records from Voyager to Vufind; it can also copy XML records from a file or OAI harvest if given an appropriate XSL transform file.  The example below describes how we can use _v2v_ to duplicate records from a Content DM collection into a Vufind Solr index.


# Simple OAI harvest #

The [v2v](http://ivy2maven2.littleware.googlecode.com/hg/webstart/vygr2vfnd.jnlp)
tool implements a simple OAI to Solr pipeline which pulls records from an OAI
server, applies an XSL transform to convert each record to a Solr document,
then posts each document up to the Solr server from which vufind pulls its data.

Before we prepare our Vufind OAI cake, we need to assemble our ingredients.
The first two ingredients are easy to acquire.
  * OAI harvest URL: We'll harvest OAI records from Auburn University Libraries' [E.B. Sledge collection](http://diglib.auburn.edu/collections/ebsledge/).  The [ContentDM](http://www.contentdm.org/) (CDM) server that hosts the Sledge collection allows OAI harvest at http://content.lib.auburn.edu/cgi-bin/oai.exe?verb=ListRecords&metadataPrefix=oai_dc&set=ebsledge
  * Vufind Solr URL: We'll deposit the records we copy from Content-DM into the [Solr](http://lucene.apache.org/solr/) index at http://devcat.lib.auburn.edu:8080/solr/ for Auburn's developer vufind server at http://devcat.lib.auburn.edu

We must prepare our third ingredient by hand - an XSL transform file with rules that convert an OAI record from the CDM Sledge collection to a Solr document to add into Vufind's index.  We can extract a sample OAI record like the following from the OAI data at http://content.lib.auburn.edu/cgi-bin/oai.exe?verb=ListRecords&metadataPrefix=oai_dc&set=ebsledge
```
        <oai_dc:dc 
           xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
           xmlns:dc="http://purl.org/dc/elements/1.1/"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/
           http://www.openarchives.org/OAI/2.0/oai_dc.xsd"> 
          <dc:title>China diary</dc:title> 
          <dc:description>In this diary, written in 1946, Eugene B. Sledge observes that civilians have no idea of what the front-line combat infantrymen have faced; all the civilians see are medals and newspaper articles. Sledge says that he might not have boarded the transport U.S.S. President Polk if he'd known what combat was like. He then compares the transport to a slave ship with the crowded conditions and stinking air.</dc:description> 
          <dc:creator>Sledge, Eugene B.</dc:creator> 
          <dc:publisher>Auburn University Libraries</dc:publisher> 
          <dc:date>1946-01-00</dc:date> 
          <dc:coverage>1940s</dc:coverage> 
          <dc:coverage>Pacific Ocean</dc:coverage> 
          <dc:subject>Sledge, E. B. (Eugene Bondurant), 1923-; World War, 1939-1945 -- Veterans; Marines -- United States; Combat; Infantry; World War, 1939-1945 -- Social aspects; World War, 1939-1945 -- Transportation; Transports; Soldier's writings, American</dc:subject> 
          <dc:subject>U.S.S. President Polk</dc:subject> 
          <dc:subject>Geography &amp; Environment -- Human Environment -- Military Installations; Government &amp; Politics -- Military; History -- 1946-1987: 
Post-World War II and the Era of Civil Rights; Peoples -- Military Life</dc:subject> 
          <dc:format>JPEG</dc:format> 
          <dc:type>Text</dc:type> 
          <dc:identifier>RG 96-38, box 1, folder 1</dc:identifier> 
          <dc:source>Eugene Sledge papers</dc:source> 
          <dc:source>Auburn University Libraries. Special Collections and Archives.</dc:source> 
          <dc:language>eng</dc:language> 
          <dc:rights>This image is the property of the Auburn University Libraries and is intended for non-commercial use. Users of the image are asked to acknowledge the Auburn University Libraries. For information about obtaining high-resolution copies of this and other images in this collection, please contact the Auburn University Libraries Special Collections &amp; Archives Department at archive@auburn.edu or (334) 844-1732.</dc:rights> 
          <dc:contributor>Caudle, Dana M.</dc:contributor> 
          <dc:identifier>http://content.lib.auburn.edu/u?/ebsledge,3</dc:identifier> 
        </oai_dc:dc> 
```

The Solr index on devcat accepts XML documents that obey the index's schema.  Our Solr server publishes its schema.xml file online at http://devcat.lib.auburn.edu:8080/solr/biblio/admin/file/?file=schema.xml  We can see from the list of fields at the end of the schema that we can encode the above CDM record as the following CDM document:
```
<doc>
<field name="id">Sledge3</field>
<field name="url">http://content.lib.auburn.edu/u?/ebsledge,3</field>
<field name="title">China Diary</field>
<field name="author">Sledge, Eugene B.</field>
<field name="topic">Sledge, E. B. (Eugene Bondurant), 1923-</field>
<field name="fulltopic">Sledge, E. B. (Eugene Bondurant), 1923-</field>
<field name="topic">World War, 1939-1945 -- Veterans</field>
<field name="fulltopic">World War, 1939-1945 -- Veterans</field>
<field name="topic">Marines -- United States</field>
<field name="fulltopic">Marines -- United States</field>
<field name="topic">Combat</field>
<field name="fulltopic">Combat</field>
<field name="topic">Infantry</field>
<field name="fulltopic">Infantry</field>
<field name="topic">World War, 1939-1945 -- Social aspects</field>
<field name="fulltopic">World War, 1939-1945 -- Social aspects</field>
<field name="topic">World War, 1939-1945 -- Transportation</field>
<field name="fulltopic">World War, 1939-1945 -- Transportation</field>
<field name="topic">Transports</field>
<field name="fulltopic">Transports</field>
<field name="topic">Soldier's writings, American</field>
<field name="fulltopic">Soldier's writings, American</field>
<field name="topic">U.S.S. President Polk</field>
<field name="fulltopic">U.S.S. President Polk</field>
<field name="topic">Geography &amp; Environment -- Human Environment -- Military Installations</field>
<field name="fulltopic">Geography &amp; Environment -- Human Environment -- Military Installations</field>
<field name="topic">Government &amp; Politics -- Military</field>
<field name="fulltopic">Government &amp; Politics -- Military</field>
<field name="topic">History -- 1946-1987: Post-World War II and the Era of Civil Rights</field>
<field name="fulltopic">History -- 1946-1987: Post-World War II and the Era of Civil Rights</field>
<field name="topic">Peoples -- Military Life</field>
<field name="fulltopic">Peoples -- Military Life</field>
<field name="description">
In this diary, written in 1946, Eugene B. Sledge observes that civilians have no idea of what the front-line combat infantrymen have faced; all the civilians see are medals and newspaper articles. Sledge says that he might not have boarded the transport U.S.S. President Polk if he'd known what combat was like. He then compares the transport to a slave ship with the crowded conditions and stinking air.
</field>
<field name="format">Electronic</field>
<field name="collection">Eugene Sledge</field>
<field name="building">Auburn University Digital Library</field>
<field name="publisher">Auburn University Libraries</field>
<field name="allfields">
          China diary 
          In this diary, written in 1946, Eugene B. Sledge observes that civilians have no idea of what the front-line combat infantrymen have faced; all the civilians see are medals and newspaper articles. Sledge says that he might not have boarded the transport U.S.S. President Polk if he'd known what combat was like. He then compares the transport to a slave ship with the crowded conditions and stinking air. 
          Sledge, Eugene B. 
          Auburn University Libraries 
          1946-01-00 
          1940s 
          Pacific Ocean 
          Sledge, E. B. (Eugene Bondurant), 1923-; World War, 1939-1945 -- Veterans; Marines -- United States; Combat; Infantry; World War, 1939-1945 -- Social aspects; World War, 1939-1945 -- Transportation; Transports; Soldier's writings, American 
          U.S.S. President Polk 
          Geography &amp; Environment -- Human Environment -- Military Installations; Government &amp; Politics -- Military; History -- 1946-1987: Post-World War II and the Era of Civil Rights; Peoples -- Military Life 
          JPEG 
          Text 
          RG 96-38, box 1, folder 1 
          Eugene Sledge papers 
          Auburn University Libraries. Special Collections and Archives. 
          eng 
          This image is the property of the Auburn University Libraries and is intended for non-commercial use. Users of the image are asked to acknowledge the Auburn University Libraries. For information about obtaining high-resolution copies of this and other images in this collection, please contact the Auburn University Libraries Special Collections &amp; Archives Department at archive@auburn.edu or (334) 844-1732. 
          Caudle, Dana M. 
          http://content.lib.auburn.edu/u?/ebsledge,3
</field>
</doc>
```

The **id** field is an important part of the Solr document.  Each entry in our Solr index must have a unique id.  At Auburn we avoid problems where record _1_ from collection _A_ overwrites record _1_ from collection _B_ by simply assigning each collection a unique id prefix.  The above record has id **Sledge3**, and every other record from the Sledge collection will have a "Sledge" prefix in its _id_ field.

There are other fields of interest in our Solr document.  Our vufind catalog implements a location facet with the **building** field, and a location sub-facet with the "collection" field, so we hard code every Sledge document to have an "Auburn University Digital Library" building and "Eugene Sledge" collection.  We populate both **topic** and **fulltopic** fields for subject entries, because vufind's display engine uses each field for slightly different purposes for MARC-based records from other collections, so the code expects both fields to be there.  The **allfields** entry is a catch-all that helps vufind implement Google-style search.

The XSL transform file we write defines a set of rules that convert the Dublin core OAI metadata to its corresponding Solr document representation.  We'll use the following XSLT rules in this example.
```
<?xml version="1.0" encoding="UTF-8" ?>


<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     >
    <xsl:output method="xml" indent="yes"/>
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


    <xsl:template match="/oai_dc:dc">
        <doc>
            <xsl:apply-templates select="*" />
            <field name="format">Electronic</field>
            <field name="collection">Eugene Sledge</field>
            <field name="building">Auburn University Digital Library</field>
            <field name="publisher">Auburn University Libraries</field>
            <field name="topic">Eugene Sledge</field>
            <field name="fulltopic">Eugene Sledge</field>
            <field name="description">
                <xsl:for-each select="dc:description">
                    <xsl:value-of select="." />
                    <xsl:text>

                    </xsl:text>
                </xsl:for-each>
            </field>
            <field name="allfields">
                <xsl:for-each select="*">
                    <xsl:value-of select="." />
                    <xsl:text>
                    </xsl:text>
                </xsl:for-each>
            </field>
        </doc>
    </xsl:template>

    <xsl:template match="dc:identifier">
        <xsl:variable name="auId" select='substring-after(.,"http://content.lib.auburn.edu/u?/ebsledge,")'/>
        <xsl:if test='$auId != ""'>
            <field name="id">SLEDGE<xsl:value-of select='$auId'/></field>
            <field name="url">
                <xsl:value-of select="." />
            </field>
            <field name="thumbnail">http://content.lib.auburn.edu/cgi-bin/thumbnail.exe?CISOROOT=/ebsledge&amp;CISOPTR=
                <xsl:value-of select='$auId'/>
            </field>
        </xsl:if>
    </xsl:template>


    <xsl:template match="dc:creator">
        <field name="author">
            <xsl:value-of select="." />
        </field>
    </xsl:template>

    <xsl:template name="split">
        <xsl:param name="subject"/>
        <xsl:variable name="first" 
              select='normalize-space(substring-before($subject,";"))'
                   />
        <xsl:variable name='rest' select='normalize-space(substring-after($subject,";"))'/>
        <xsl:if test='$first'>
            <field name="topic">
                <xsl:value-of select='$first'/>
            </field>
           <field name="fulltopic">
                <xsl:value-of select='$first'/>
            </field>	
        </xsl:if>

        <xsl:if test='$rest'>
            <xsl:call-template name='split'>
                <xsl:with-param name='subject' select='$rest'/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test='not($rest)'>
            <field name="topic">
                <xsl:value-of select='$subject'/>
            </field>
            <field name="fulltopic">
                <xsl:value-of select='$subject'/>
            </field>
        </xsl:if>

    </xsl:template>

    <xsl:template match="dc:subject">
        <xsl:if test="normalize-space(.)">
            <xsl:call-template name="split">
                <xsl:with-param name="subject" select="."/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dc:title[1]">
        <field name="title">
            <xsl:value-of select="." />
        </field>
    </xsl:template>

    <xsl:template match="dc:description-oldway">
        <field name="description">
            <xsl:value-of select="." />
        </field>
    </xsl:template>

</xsl:stylesheet>
```

Once we save our XSLT file, we're ready to try to import some records.
  * Launch [v2v](http://ivy2maven2.littleware.googlecode.com/hg/webstart/vygr2vfnd.jnlp)
  * Click on the OAI tab
  * Fill in the form with the appropriate data:
    * SOLR URL: http://devcat.lib.auburn.edu:8080/solr/biblio
    * XSL File: PathToYourFile.xsl
    * OAI Base URL: http://content.lib.auburn.edu/cgi-bin/oai.exe
    * Min Record Date: 1900/01/01
    * Max Record Date: 2200/01/01
    * Oai Set Name: ebsledge
    * Metadata prefix: oai\_dc
  * The _Test_ button harvests a subset of OAI records, applies the XSL transform, and displays the transform result without posting anything to Solr.
  * The _Import_ button harvests all the OAI records within the date range, transforms them, and posts them to the Solr server.

# Notes #

Other Auburn CDM collections - just replace "ebsledge" with one of the following:
```
armyaviation  collect      forestry    pianobench  theatre01
aulphoto      custom       gloms       postcard    theatre1.delete
aunumphoto    ebsledge     gloms-1932  poultry     urban
autest        eddier       gloms1980   qdc         vetmed1
bot           eoa          gosse       raptor      vetmed2
bottest       findingaids  ladc        sage        vetmed3
bscmmgc       flora        leavins     smof        vetmed4
civil         football     maps        splans      vratest.delete
```

Use Solr's _bin/post.sh_ script to post individual records to Solr, and view the Solr server's response.
It's easy to post to Solr if you have access to a
Linux or MacOS terminal and you're comfortable with that kind of thing.

  1. Put the problem record in a file "testRecord.xml" with _add_ root element:
```
                         <add>
                          <doc ...>
                             bla bla bla ---  the document
                          </doc>
                          </add>
```

  1. Use the 'post.sh' script in Solr/bin to post testRecord.xml to the server:
```
                             $ post.sh testRecord.xml
```
> > something like that.
```

$ cat post.sh
#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. ...
FILES=$*
URL=http://localhost:8080/solr/biblio/update

for f in $FILES; do
  echo Posting file $f to $URL
  curl $URL --data-binary @$f -H 'Content-type:text/xml; charset=utf-8'
  echo
done

#send the commit command to make sure all the changes are flushed and visible
echo "Issueing commit to $URL"
curl $URL --data-binary '<commit/>' -H 'Content-type:text/xml; charset=utf-8'
echo

```