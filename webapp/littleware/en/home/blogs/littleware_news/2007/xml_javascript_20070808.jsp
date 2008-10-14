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
    <title>XML/XSLT and Javascript</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/xml_javascript_20070808.jsp"/>
    <id>urn:uuid:A8D9597E-52C7-4F3D-82B3-95BBCFB515C6</id>
    <updated>2007-08-08T18:31:02Z</updated>
    <summary type="xhtml">Experiences with XML/XSLT and javascript.
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Inheritance with Delegation"
      last_modified="08/08/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="XML/XSLT and Javascript">
<di:timestamp year="2007" month="08" day="08" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro>
<p>
In the work in progress that is this littleware web site,
we use an alphabet soup of technologies to put together
the page that finally gets rendered by a client's browser.
On the server side we use JSP and JSF pages that 
(with the support of underlying java code) deliver
XML, XSLT, CSS, and javascript to the client's browser.
The goal is to achieve the coveted division of responsibilities
between content providers, site managers, programmers,
and graphic designers.
</p>
</di:intro>
<di:section topic="Web Site Division of Labor">

<p>
A web site with a good division of labor 
is setup so that a programmer doesn't wind up 
trying to do graphics design, a graphics designer
doesn't have to write scripts, a content provider
doesn't have to worry about where in the web site
his document should be deployed, and a site
manager doesn't have to worry about documents 
having an inconsistent look and feel.
We tackle this problem for the littleware web site 
by authoring content in XML, deploying the XML to a
J2EE webapp programmed to manage basic backend functionality
(security, database access, error recovery, logging)
and to pair each XML file with an XSL stylesheet that in
turn generates XHTML with embedded CSS, applet, and javascript links.
</p>
<p>
A content author delivers content in a simple XML format
resembling this:
<pre class="code">
<code>

   &lt;?xml version="1.0" encoding="UTF-8"?&gt;
   
   &lt;gen:view ...&gt;
       &lt;gen:support ...&gt;
             &lt;gen:rssinfo&gt; ...

             &lt;/gen:rssinfo&gt; 
       &lt;/gen:support&gt;
             
       &lt;gen:document&gt;
         Generic XML document with simple embedded HTML markup.
       &lt;/gen:document&gt;
   &lt;/gen:view&gt; 

</code>
</pre>
</p>
<p>
A site manager decides where that content ought to lie in
the site, and places the document there, where the backend engine
automatically assigns an XSL style sheet to the document
(via the xml-stylesheet tag).  
The XSL stylesheet and its supporting CSS and javascript
files are authored by a graphics designer.
The client browser
applies the XSL style sheet rules to convert the XML document
to an XHTML page with menus, banners, navigation, and
links to javascript files, java applets, and 
CSS style sheets, and your client gets a nice web page.
</p>
</di:section>
<di:section topic="Browser XML Problems">
<p>
This all sounds great in theory, and it has worked pretty
well for us (except we need a better graphics designer than me).
We have not completely implemented the pipeline described, but
if you view the source of this page you'll find that the
document is XML with an xml-stylesheet link to an XSL file.
However, there are a couple issues resulting from the current
state of browser technology.
</p>
<p>
The first problem we ran into had to do with
support for acyclic graphs of XSL stylesheet
xsl:import dependencies.
The xsl:import directive allows an XSL file
to import rules from another XSL file.
The MacOS 10.4 version of Safari would crash
if an XSL file below the root stylesheet 
used xsl:import.  When we went to report this bug
we found that the WebKit developers had already
fixed it in newer builds, and Safari-3 no longer
has this problem.
</p>
<p>
A problem we have just ran into is that Firefox and IE do not
treat the XHTML page resulting from XSLT processing
of XML the same way as though the server delivered the XHTML
page directly in the sense that CSS style rules and embedded
javascript get executed the same way in either case.
Safari (the Macintosh browser) and Internet Explorer do this well, but
Firefox does not execute javascript in the XSL-generated document.
For example, Firefox does not render "Hello" for the XSL-generated
XHTML page with the following block:

<pre class="code">
<code>
   &lt;script ...&gt;
   &lt;!--
   // &lt;![CDATA[
        document.writeln( &quot;Hello&quot; );
   // ]]&gt;
   // --&gt;
   &lt;/script&gt;
</code>
</pre>
</p>
<p>
We ran into this problem with Firefox when we attempted
to use javascript to dynamically generate object/embed
blocks for applets.
All three browsers (Firefox, IE, and Safari) will allow
us to attach javascript events  (onclick, hyperlink to javascript, etc.)
to DOM elements, so the problem is not a show stopper
for our current site, but it is something we need to keep in
mind going forward.
We could work around this problem by applying the XSL
transform on our server, but it would be nice to offload
that work to the client - especially if our site ever gets busy.
Hopefully the good people at mozilla.com will 
get a chance to take a look at the bug report we submitted.
</p>


</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
