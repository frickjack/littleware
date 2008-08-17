<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ page import="java.util.*,javax.security.auth.*,littleware.base.*,littleware.security.auth.*,littleware.security.*,littleware.asset.*,littleware.web.beans.*,littleware.web.pickle.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>


<gen:document 
      title="Asset View"
      last_modified="12/07/2006"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

    <%
     try {
        out.println ( "<h3> Session Attribute Names </h3>" );
        Enumeration enum_param = session.getAttributeNames ();
        while ( enum_param.hasMoreElements () ) {
            String s_name = (String) enum_param.nextElement ();
            out.print ( s_name );
            out.println ( " <br />\n" );
         }
         Subject subj_active = (Subject) session.getAttribute ( "javax.security.auth.subject" );
         if ( null != subj_active ) {
            out.println ( "<p> Security subject is: " + 
                            subj_active.getPrincipals ( LittleUser.class )
                          + "</p>"
                       );
          }

        out.println ( "<h3> Request Parameter Names </h3>" );
        enum_param = request.getParameterNames ();
        while ( enum_param.hasMoreElements () ) {
            String s_name = (String) enum_param.nextElement ();
            String[] v_values = request.getParameterValues ( s_name );
            out.print ( s_name );
            out.print ( " -&lt " );
            out.print ( v_values );
            out.println ( " <br />\n" );
         }
    } catch ( Exception e ) {
        out.println ( "<gen:error> Caught unexpected: <br />" );
        out.println ( "<verbatim>\n\n" );
        out.println ( XmlSpecial.encode ( BaseException.getStackTrace ( e ) ) );
        out.println ( "\n\n</verbatim></gen:error>" );
    }
    %>

</gen:document>

