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
       <c:param name="sidemenu_select" value="web_services" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Web Services"
      last_modified="09/23/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Littleware Web Services </h3>

<p>
We are working to develop a set of software services
that a client may freely access to add functionality
to his site or client.
We intend for our clients to use these free services for demo
and test purposes, since our free server does not
reserve bandwidth or guarantee availability.
A client that requires better quality of service should
either run his own copy of the littleware software stack,
or reserve bandwidth on our littleware hosting server.
<a href="mailto:${lw_defaults.defaults['contact_email']}">Contact</a> us 
for support.
</p>
<p>
The littleware APIs are currently only available for remote
access via java remote method invocation (RMI).
We plan to provide SOAP and REST based bindings in the near future.
This <em>Toolbox</em> applet (must login to run the applet)
provides a suite GUI tools that interact with the
littleware asset repository using RMI to access
the <a href="/littleware/lib/doc/api">littleware API</a>.
<br />
<applet 
        id="toolbox"
        code="littleware.web.applet.Toolbox.class"
	width="400"
	height="150"
      >
    <param name="session_uuid" value="${lw_user.helper.session.objectId}" />
  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</applet>

<dl>
<dt> Log Viewer </dt>
   <dd> Provides a console to the log messages published by
      the applet. </dd>

<dt> Asset Browser </dt>
    <dd>Simple browser for viewing assets in the littleware repository.
    The browser allows us to easily
    navigate between connected assets, and includes tools
    for editing, creating, and deleting assets.
    </dd>

<dt> Group Tool </dt>
    <dd> A customized editor tool for managing Group type assets
    under the logged in user&apos;s GroupFolder asset.
    </dd>
</dl>

</p>
<p>
java.policy
</p>
<p>
The littleware asset system supports various types of asset.
Our current development effort is focused on developing tools
and APIs supporting authentication, group management, 
access control, and synchronization.
These tools primarily manage
<a href="/littleware/lib/doc/api/littleware/security/LittleUser.html">User</a>,
<a href="/littleware/lib/doc/api/littleware/security/LittleGroup.html">Group</a>,
<a href="/littleware/lib/doc/api/littleware/security/LittleAcl.html">Acl</a>,
and
<a href="/littleware/lib/doc/api/littleware/asset/Asset.html">generic</a>
type assets.
The source code for the Toolbox applet and other applications is
open source, and available for 
<a href="/littleware/en/toolbox/src/home.jsp">download</a>
</p>

</gen:document>

</gen:view>
