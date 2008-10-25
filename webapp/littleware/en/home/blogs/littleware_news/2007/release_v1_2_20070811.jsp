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
    <title>Littleware release v1.2</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/release_v1_2_20070811.jsp"/>
    <id>urn:uuid:38F7AB8D-47F5-4F58-BF96-746A9B7C6D28</id>
    <updated>2007-08-11T18:31:02Z</updated>
    <summary type="xhtml">Littleware release v1.2
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Littleware release v1.2"
      last_modified="08/11/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="Littleware Release v1.2">
<di:timestamp year="2007" month="08" day="11" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro id="intro">
<p>
Today we upgraded the littleware library underpinning
this site to version 1.2.  
Most of the differences between this release and the previous
release have to do with the introduction of new Swing UI
components.
The main visible difference
on this site is the introduction of the group-maangement applet
under the 
<a href="/littleware/en/toolbox/services/home.jsf">WebServices Toolbox</a>.
</p>
</di:intro>
<di:section id="sec_changes" topic="Release Changes">
<p>
Here is a partial list of the changes in this release.
</p>
<ul>
<li> Introduction of com.nexes.wizard Wizard framework </li>
<li> Introduction of CreateAssetWizard </li>
<li> Rework AssetModel to register LittleListeners
     for common LittleEvents rather than retrofit
     PropertyChangeListener. </li>
<li> Introduction of JGroupList editor and JGroupsUnderParentEditor 
     components in littleware.apps.swingclient </li>
<li> Introduction of simple build.xml ANT build script </li>
<li> Move to use &lt;object&gt; and &lt;embed&gt; tags rather
      than &lt;applet&gt;
      via an xhtml:applet XSL rewrite rule in en/lib/xsl/general.jsp.
      </li>
<li> rearrange javascript files to be more modular </li>
</ul>

</di:section>

<di:section id="sec_next" topic="What's Next">
<p>
I'm not sure what we'll get to for the next release,
but there is plenty of work to be done.
Hopefully we'll be able to put the following together
for the next release in a month or so.
We'll see how it goes.
</p>
<ul>
<li> Move server-only classes into their own subpackages </li>
<li> Extend ANT build.xml file </li>
<li> GUICE IOC integration </li>
<li> Application user may run that edits the user java.policy file
       to grant trust to jar files loaded from frickjack.com. </li>
<li> Add Locale to littleware Session, and extend i18n, l10n support </li>
<li> Extend use of .properties files in UI configuration </li>
<li> Migrate to EJB3.0 appserver. </li>
<li> Rework JSimpleAssetView SWING component </li>
<li> Introduce ACL-editor Swing Applet </li>
<li> Add SOAP and REST bindings </li>
<li> Add a new acl-check JSP tag, and an access-denied error-page. 
          </li>
<li> Rework security-related Exception hierarchy </li>
<li> Begin work on backend MySQL support </li>
</ul>
               
</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
