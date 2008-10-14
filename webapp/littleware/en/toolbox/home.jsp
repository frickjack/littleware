<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.littleware.com/xml/taglib/2006/general" 
               prefix="lw" %>  

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<jsp:useBean id="lw_defaults" class="littleware.web.beans.DefaultsBean" />
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="toolbox" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="home" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Toolbox Home"
      last_modified="09/23/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> <img src="/littleware/lib/icons/geronimo/tools_16.gif" /> Toolbox </h3>

<p>
Welcome to the Littleware toolbox. 
Here you will find documentation, demos, and downloads
for our various products and services.
</p>

<applet 
        id="toolbox"
        codebase="/littleware/lib/jar" 
        archive="littleware_v1.0b.jar,java-getopt.jar,mailapi.jar,mail.jar" 
        code="littleware.web.applet.Toolbox.class"
        name="toolbox"
	width="400"
	height="150"
      >
    <param name="session_uuid" value="${lw_user.helper.session.objectId}" />
    <!--
    <param name="server_host" value="localhost" />
    -->
    <param name="server_port" value="1259" />

    <param name="draggable" value="true" />
  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</applet>

<dl>
  <dt> <b><a href="vita.jsp">software development</a> </b></dt>
      <dd> Work with a littleware <a href="./vita.jsp">developer</a> 
       to design and build
       custom software solutions for 
       data and workflow management problems.
        </dd>
     <br />
  <dt> <b><a href="./web_services.jsp">web services</a> </b></dt>
      <dd> 
       Integrate <a href="./web_services.jsp">services</a> 
       provided by littleware via 
       REST, SOAP, javascript, and RMI
       to add functionality to your web site or 
       application.
       </dd>
     <br />
  <dt> <b><a href="./src/home.jsp">open source support</a> </b></dt>
       <dd>
       Download the open source littleware 
       <a href="./src/home.jsp">code base</a>, and
       run littleware services on your own servers.
       We offer support packages to get you up and running
       on your own server, and to develop tools to 
       support custom workflows.
       </dd>
     <br />
  <dt><b> Auburn, AL area tech support </b></dt>
        <dd> Work with littleware support to maintain wired and wireless
         networks and workgroup servers in your home or office. </dd>
</dl>

<p>
<a href="mailto:${lw_defaults.defaults['contact_email']}">Contact</a> us 
for details.
</p>
       
</gen:document>

</gen:view>
