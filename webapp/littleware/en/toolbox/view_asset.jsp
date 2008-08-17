<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>
<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom entry -->
  <entry>
    <title> /en/toolbox/view_asset.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/toolbox/view_asset.jsp"/>
    <summary>Simple XML view of an asset with some
             self-referencing navigation. </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com </rights>
  </entry>
  <jdoc:param><jdoc:name>object_id</jdoc:name>
              <jdoc:description> 
            the UUID id of the asset to view 
            </jdoc:description>
  <jdoc:param>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

  <jsp:directive.page contentType="text/xml;charset=UTF-8" 
                 import="java.util.*,littleware.base.*,littleware.security.auth.*,littleware.security.*,littleware.asset.*,littleware.web.beans.*,littleware.web.pickle.*,littleware.apps.addressbook.*"  
        />
  <jsp:output omit-xml-declaration="false" />



<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns="http://www.w3.org/1999/xhtml"
      xmlns:jsp="http://java.sun.com/JSP/Page"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="toolbox" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8" />
</gen:websupport>

<gen:document 
      title="Asset View"
      last_modified="02/01/2007"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:asset="http://www.littleware.com/xml/namespace/2006/asset"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<c:choose>
<c:when test="${lw_user.authenticatedName == null}">
   <gen:error>
      <p>
       Must <a href="../account/login.jsf">login</a> to use view_asset.
      </p>
   </gen:error>
</c:when>
<c:when test="${!empty param.object_id}">
    <jsp:scriptlet>
     // <![CDATA[
     try {
         SessionBean   bean_user = (SessionBean) session.getAttribute( "lw_user" );
         if ( null == bean_user ) {
            out.println ( "<h3> Session Attribute Names </h3>" );
            Enumeration enum_param = session.getAttributeNames ();
            while ( enum_param.hasMoreElements () ) {
                String s_name = (String) enum_param.nextElement ();
                out.print ( s_name );
                out.println ( " <br />\n" );
             }
         } else {
             SessionHelper m_helper = bean_user.getHelper ();
             String        s_asset_id = request.getParameter ( "object_id" );
             UUID          u_asset = UUIDFactory.parseUUID ( s_asset_id );
             AssetSearchManager m_search = m_helper.getService ( ServiceType.ASSET_SEARCH );
             Asset         a_result = m_search.getAsset ( u_asset );
             PickleMaker<Asset>  pickle_asset = PickleType.XML.createPickleMaker ();
             pickle_asset.pickle ( a_result, out );

             Map<String,UUID>  v_from = m_search.getAssetIdsFrom ( a_result.getObjectId (), null );
             // ]]>
             request.setAttribute ( "v_from", v_from.entrySet () );
     </jsp:scriptlet>
             <hr />
             <p> Assets linking FROM <jsp:expression> s_asset_id </jsp:expression>: </p>
           <ul>
     <!--
             for ( Map.Entry<String,UUID> map_entry : v_from.entrySet () ) {
     -->
             <c:forEach items="${v_from}" var="map_entry">
                 <li>
                  <!--
                     <asset:link aref="<%= map_entry.getValue () %>"> 
                          <%= map_entry.getKey () %> </asset:link> 
                  -->
                     <asset:link aref="${map_entry.value}"> 
                          <c:out value="${map_entry.key}" /> </asset:link> 
                 </li>
             </c:forEach>
          </ul>
    <jsp:scriptlet>
     // <![CDATA[
          } // end-else
    } catch ( Exception e ) {
        out.println ( "<gen:error> Caught unexpected: <br />" );
        out.println ( "<verbatim>\n\n" );
        out.println ( XmlSpecial.encode ( BaseException.getStackTrace ( e ) ) );
        out.println ( "\n\n</verbatim></gen:error>" );
    }
    // ]]>
    </jsp:scriptlet>
</c:when>
<c:otherwise>
<p>
No data requested.
</p>
</c:otherwise>
</c:choose>

</gen:document>

</gen:view>

</jsp:root>
