<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>
<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom documentation entry -->
  <entry>
    <title> /en/toolbox/src/todo.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/toolbox/src/todo.jsp"/>
    <summary>Development TODO list.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

  <jsp:directive.page 
          contentType="text/xml;charset=UTF-8" 
      />
  <jsp:output omit-xml-declaration="false" />
  <jsp:text>

  </jsp:text>

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="toolbox" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="todo" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Littleware TODO"
      last_modified="02/06/2007"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns:task="http://www.littleware.com/xml/taglib/2007/task" 
      xmlns:atom="http://www.w3.org/2005/Atom"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Littleware Developer TODO </h3>

<!-- 
   Just inline this for now.
   Later split each task into separate entries,
   and associate an atom-feed with the TODO.
-->
<task:tasklist>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> Generate more documentation </task:summary>
   <task:assignto by="Reuben" date="02/07/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Need documentation on how to setup a littleware server,
        and how to code a littleware client.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
   </task:history>
</task:task>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> UI group-edit tools </task:summary>
   <task:assignto by="Reuben" date="02/07/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Need UI tools to manipulate groups.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
     <task:comment by="Reuben" date="06/07/2007">
         Simple group-editing tool deployed for local1260 hosting app.
     </task:comment>
   </task:history>
</task:task>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> UI acl-edit tools </task:summary>
   <task:assignto by="Reuben" date="02/07/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Need UI tools to manipulate acls.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
     <task:comment by="Reuben" date="06/07/2007">
         Simple ACL-edit tool deployed as applet for local1260 hosting.
     </task:comment>
   </task:history>
</task:task>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> REST and SOAP tools </task:summary>
   <task:assignto by="Reuben" date="02/07/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Need REST and/or SOAP client bindings in addition to the RMI.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
   </task:history>
</task:task>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> File system assets </task:summary>
   <task:assignto by="Reuben" date="02/07/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Need to associate an asset with a directory.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
     <task:comment by="Reuben" date="07/07/2007">
         littleware.apps.filebucket package integrated into server
     </task:comment>
   </task:history>
</task:task>

<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> HttpUnit integration </task:summary>
   <task:assignto by="Reuben" date="02/08/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Integrate HttpUnit based testing of the web site JSPs.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="02/07/2007">
         I&apos;m running behind!
     </task:comment>
   </task:history>
</task:task>
<task:task>
   <task:requester>Reuben</task:requester>
   <task:summary> GUICE integration </task:summary>
   <task:assignto by="Reuben" date="07/08/2007"> Reuben </task:assignto>
   <task:status>pending</task:status>
   <task:description>
        Integrate Google GUICE IOC based dependency injection into codebase.
   </task:description>
   <task:history>
     <task:comment by="Reuben" date="07/08/2007">
         Pending
     </task:comment>
   </task:history>
</task:task>

</task:tasklist>

       
</gen:document>

</gen:view>

</jsp:root>
