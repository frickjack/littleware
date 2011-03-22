<%@page contentType="text/xml" pageEncoding="UTF-8"%><?xml version="1.0" encoding="UTF-8" ?>
<?xml-stylesheet type="text/xsl" href="verify.xsl" ?>

<verify>Ok</verify>

<%
// No sense in keeping the session around for simple XML services
session.invalidate();
%>
