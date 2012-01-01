<%-- 
    Document   : browserView
    Created on : Nov 26, 2011, 6:37:12 PM
    Author     : pasquini
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        

        <link rel="stylesheet" href="http://yui.yahooapis.com/3.4.1/build/cssreset/cssreset.css" type="text/css" />
        <link rel="stylesheet" href="http://yui.yahooapis.com/3.4.1/build/cssbase/cssbase.css"  type="text/css" />
        <link rel="stylesheet" href="http://yui.yahooapis.com/3.4.1/build/cssfonts/cssfonts.css" type="text/css" />
        <link rel="stylesheet" href="http://yui.yahooapis.com/3.4.1/build/cssgrids/cssgrids.css" type="text/css" /> 

        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/littleware/little.css" />
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/littleware/littleTree.css" />
        
        <style>
            
    body {
        margin: auto; /* center in viewport */
        width: 960px;
    }   
    
         </style>
    </head>
    <body>
        
    <div class="yui3-g">
        <div class="yui3-u-5-24">
            <div id="navTree" class="littleTree">
                <h3>Tree</h3>
                
                <ul class="littleTree">
                    <li>A</li>
                    <li>B
                        <ul class="littleTree littleTreeClosed">
                            <li>A</li>
                            <li>B</li>
                            <li>C</li>
                        </ul>
                        </li>
                    <li>C</li>
                    <li>D</li>
                </ul>
            </div>

        </div>
        <div class="yui3-u-19-24">
        <h1>Hello World!</h1>
        </div>
    </div>
        
    <script type="text/x-template" id="histogramPopup">
        <div class="histogramPopup">
            <div class="yui3-widget-hd"> <a href="#">x close</a> <h3>Title</h3></div>
            <div class="yui3-widget-bd">
                <img src="/resources/images/histogramExample.svg" alt="histogram" />
            </div>
            <div class="yui3-widget-ft"><a href="#">popup</a></div>
        </div>
    </script>  
        
<script src="http://yui.yahooapis.com/3.4.1/build/yui/yui-min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/littleware/littleYUI.js" charset="utf-8"></script>

<script>
// Create a YUI sandbox on your page.
YUI().use('node', 'event', function (Y) {
    // The Node and Event modules are loaded and ready to use.
    // Your code goes here!
});
</script>

    </body>
</html>
