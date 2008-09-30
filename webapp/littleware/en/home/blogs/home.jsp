<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=UTF-8" %>
<%@ page import="java.util.*,java.io.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="home" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_home.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="blogs" />
  </c:import>
</gen:websupport>

<gen:document 
      title="Home Index"
      last_modified="11/01/2006"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<p>

<jsp:scriptlet>
    //<![CDATA[
    // Need to set a bean to track server root
    java.io.File fh_parent = new java.io.File ( "/Library/Tomcat/webapps/" +
                                               request.getRequestURI () 
                                             ).getParentFile ();
    List<String> v_options = new ArrayList<String> ();
    for ( String s_child : fh_parent.list () ) {
      if ( (! s_child.equals ( "CVS" ))
           && (new File ( fh_parent, s_child )).isDirectory () ) {
          v_options.add ( s_child );
      }
    }
    pageContext.setAttribute ( "v_options", v_options );
    // ]]>
</jsp:scriptlet>


Available blogs under 
    <jsp:expression> request.getRequestURI () </jsp:expression>:

<ul>
<c:forEach var="s_child" items="${v_options}">
  <li><a href="./${s_child}/home.jsf"><c:out value="${s_child}" /></a></li>
</c:forEach>
</ul>
</p>

</gen:document>

</gen:view>
