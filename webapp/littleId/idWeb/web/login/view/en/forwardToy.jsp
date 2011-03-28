<%@page contentType="text/html" pageEncoding="UTF-8"%><?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE html>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

        <%
        //request.getRequestDispatcher("/login/view/en/simpleLogin.jsf" ).forward( request, response );
        %>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>

        <p>IdBean:
        <c:out value="${idBean.user.name}" />
        </p>
    </body>
</html>
