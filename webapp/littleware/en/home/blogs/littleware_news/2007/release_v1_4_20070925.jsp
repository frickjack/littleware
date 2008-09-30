<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.littleware.com/xml/taglib/2006/general"
               prefix="lw" %>

<jsp:useBean id="lw_defaults" class="littleware.web.beans.DefaultsBean" />

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="help" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_home.jspf" charEncoding="UTF-8">
       <c:param name="sidemenu_select" value="blogs" />
       <c:param name="submenu_select" value="littleware_news" />
  </c:import>
  <!-- ATOM syndication -->
  <entry 
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
      >
    <title>Littleware release v1.4</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/release_v1_4_20070925.jsp"/>
    <id>urn:uuid:9A4CCF91-2E72-423C-8BF5-6B2D84070063</id>
    <updated>2007-09-25T18:31:02Z</updated>
    <summary type="xhtml">Littleware release v1.4
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Littleware release v1.4"
      last_modified="09/25/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="Littleware Release v1.4">
<di:timestamp year="2007" month="09" day="25" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro id="intro">
<p>
Today we upgraded the littleware library underpinning
this site to version 1.4.  
Version 1.4 improves the littleware asset-browser GUI
by moving to a tabbed-pane based interface, and adding controls
to support asset create, edit, and delete operations.
The new release also reworks the littleware.web.applet package
to center on a Toolbox applet that takes advantage of the
persistence of static data between applet reloads to 
maintain session state and asset library cache data
between applet reloads.
</p>
</di:intro>
<di:section id="sec_changes" topic="changes">
<p>
Version 1.4 achieves much of its new functionality by
refactoring JGenericAssetView and JGenericAssetEditor
in the littleware.apps.swingclient package,
and introducing littleware.apps.swingclient.controller.ExtendedAssetViewController.
As implied by their names, the JGenericAssetView and JGenericAssetEditor
classes are intended to provide generic asset-view and asset-edit controls
respectively to littleware GUI designers.
One problem the designers struggled with is how to allow
subtypes of JGenericAssetView (also JGenericAssetEditor) to easily extend the 
base functionality with special controls for manipulating 
a particilar littlewre.asset.Asset subtype.
The version 1.4 release refactors JGenericAssetView so that its UI
centers on a JTabbedPane.  Subtypes of JGenericAssetView may simply
add tabs to extend the functionality of the generic Asset view.
For example, the JGroupView class extends JGenericAssetView by
simply adding to the UI a "Group Info" tab that presents the
members of the Group.
</p>
</di:section>
<di:section id="sec_next" topic="What's Next">

<p>
For the next release we will try to get to some of the
following.
</p>

<ul>
<li> GUICE IOC integration </li>
<li> Add SOAP and REST bindings </li>
<li> Migrate to Mercurial SCM system </li>
<li> Add a new acl-check JSP tag, and an access-denied error-page. 
          </li>
<li> Rework security-related Exception hierarchy </li>
<li> HttpUnit integration </li>
</ul>
               
</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
