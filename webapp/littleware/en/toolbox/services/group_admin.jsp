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
       <c:param name="submenu_select" value="groups" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Web Services"
      last_modified="07/12/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
      xmlns="http://www.w3.org/1999/xhtml"
     >

<h3> Littleware User Groups </h3>

<p>
The <a href="nowhere">littleware library</a> includes 
the littleware.security.LittleGroup interface - 
an extention of java.security.acl.Group - 
implementing the littleware.security.SecurityAssetType.GROUP asset type.
This applet provides tools for browsing, editing, and
creating groups in the asset repository under the AssetPath:
    <gen:path>/byname:home:littleware.web_home:type:littleware.USER:name:<c:out value="${lw_user.user.name}" />/GroupsFolder/</gen:path>
.
</p>

<!-- 

Frick - Firefox cannot deal with XML/XSLT + javascript.  Ugh!

<script>
//<![CDATA[

lwApplet_writeApplet ( document, //new lwApplet_Filter( true ), 
                       "littleware.web.applet.GroupTool.class",
                       200, 100,
                       new Array (
                                   { s_name: "session_uuid",
                                     s_value: "<c:out value="${lw_user.helper.session.objectId}" />"
                                    }
				    )
                        );

// ]]> 
</script>

-->



<applet 
        code="littleware.web.applet.GroupTool.class"
	width="200"
	height="100">
    <param name="session_uuid" value="${lw_user.helper.session.objectId}" />
  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</applet>


<hr />

<p>
A client may write tools in java to query and manipulate in java
by downloading the littleware source or directly accessing
<a href="/littleware/lib/jar/littleware.jar">littleware.jar</a>.
We are developing SOAP and REST based APIs that
non-java client may access.
</p>

</gen:document>

</gen:view>
