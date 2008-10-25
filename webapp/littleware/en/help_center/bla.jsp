<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<gen:topmenu 
    select="<c:out value='${param.topmenu_select}' />"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/1999/xhtml"
    >
    <gen:menuitem name="home">
       <xhtml:a href="/littleware/en/home/home.jsf">Home</xhtml:a>
    </gen:menuitem>
    <gen:menuitem name="toolbox">
       <xhtml:a href="/littleware/en/toolbox/home.jsf">Toolbox</xhtml:a>
    </gen:menuitem>
    <gen:menuitem name="help">
       <xhtml:a href="/littleware/en/help_center/home.jsf">Help Center</xhtml:a>
    </gen:menuitem>
    <gen:menuitem name="home">
       <xhtml:a href="/littleware/en/account/home.jsf">My Account</xhtml:a>
    </gen:menuitem>
  </gen:topmenu>
