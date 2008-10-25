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
    <title>Littleware release v1.3</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/release_v1_3_20070828.jsp"/>
    <id>urn:uuid:68137544-A427-4653-AF19-03525463E88C</id>
    <updated>2007-08-28T18:31:02Z</updated>
    <summary type="xhtml">Littleware release v1.3
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Littleware release v1.3"
      last_modified="08/28/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="Littleware Release v1.3">
<di:timestamp year="2007" month="08" day="28" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro id="intro">
<p>
Today we upgraded the littleware library underpinning
this site to version 1.3.  
Version 1.3 refactors the littleware java package 
hierarchy, fleshes out the ANT build script,
and bundles some command-line utilities for easy distribution.
</p>
</di:intro>
<di:section id="sec_changes" topic="changes">
<p>
The most important change in our version 1.3 release
decouples the java classes that should only be loaded by
a littleware application server from the classes that may be
loaded by littleware clients.
We accomplished the separation by simply introducing various
littleware.*.server (littleware.asset.server, 
littleware.security.server, littleware.security.auth.server, ...)
packages that we bundle into their own littleServer.jar.
The updated API docs are online
  <a href="/littleware/lib/doc/api/index.html">here</a>.
</p>
<p>
As part of our refactoring effort we also fleshed out
the ANT build script that we began developing for the v1.2
littleware release.  Our build.xml file now includes
rules that build client and server jar files, 
compile our javadoc API documentation, and
run our regression test suite.
</p>
</di:section>
<di:section id="sec_littlego" topic="littlego">
<p>
Finally, we put together our first bundle (version 0.0)
of littleware command-line utilities - littlego.zip.
Over time we intend to flesh out littlego.zip to 
include a suite of littleware rich client applications,
but for now the littlego bundle just includes the following
collection of tools.
</p>
<dl>
   <dt> colorme </dt>
   <dd>
      A Mac-specific python command-line tool for changing the background
      color of the front-most Terminal window.
      The script assumes the littleware python modules are installed
      under ~/Library/Python.
      <br />
      Ex: <br />
      <pre class="code">
	$ colorme yellow
      </pre>
   </dd>
   <dt> finder_copy </dt>
    <dd>
     A Mac specific utility that performs a file or directory copy
    via the Mac Finder.  The Finder pops up a progress bar,
    so this is a nice utility for larg data moves that may
    take a long time to complete. <br />
       Ex: 
       <pre class="code">
         $ finder_copy big_backup.tgz /Volumes/myIpod/Backup
       </pre>
    </dd>
   <dt> finder_trash </dt>
    <dd>
    A Mac specific command-line utility asks the Finder to
   move the specified file/directory to the Trash.
   You can accomplish something similar with a simple
    <em class="code">mv bla ~/.Trash/</em>,
   but the Finder will take care of renaming files in the Trash
   if two files with the same name are trashed.
    </dd>
   <dt> unix_erase </dt>
     <dd>
     A Mac-specific command installed in the user script menu
  (~/Library/Scripts/Littleware/unix_erase) that does a simple
  <em class="code">/bin/rm -rf</em> (after user confirmation)
  on the files or directories currently selected in the topmost
  Finder window and not located on the Mac disk.  This utility
  is useful for erasing files off a firewire drive or iPod
  from the Finder without having the Finder copy the data to
  the Trash folder on the main Mac disk.
     </dd>
   <dt> nothing.xsl </dt>
   <dd>
      A little pass-through xsl file that we can use to quickly
    verify the correctness of an XML file (including XHTML)
    using xsltproc. <br />
       Ex:
         <pre class="code">
	   $ xsltproc ~/Library/XML/nothing.xsl  testme.xml
	 </pre>
   </dd>
   <dt> littlego </dt>
    <dd>
     A java application launcher script that assumes littleware.jar
     and its dependency jar files are installed under ~/Library/Java.
     We currently only have three java apps registered with littlego -
     as described in the following copy of littlego's help info. 
     These tools should work find on Linux and Windows if python
     and java are installed.  We intend to write a .NET version
     of the littlego launcher for Windows in the future, so
     clients won't need python installed, but the core
     apps will still require java.
      <br />
       <pre class="code">

Java application launcher for littleware jar-based apps
where jars are installed in ~/Library/Java.

    littlego [-h] [-v] &lt;application&gt; [app-options]
         -h == show this documentation
         -v == enable verbose logging of this script's actions

The following apps are available:

    tabs == replace leading tabs with spaces
            for STDIN or from the list
            of files given on the command line
            (uses the OstermillerUtils java library).
            Pass the -h option to get detailed tabs help.

           ex: $ littlego tabs -h
               $ littlego tabs fileWithAnnoyingTabs.java

    xmlcoder == encode/decode XML-special characters (&lt;&gt;&quot;&apos;)
           from stdin or for the clipboard.
           Pass the -h option to get detailed help.

          ex: $ littlego xmlcoder -h
              $ echo "&lt;encode-this&gt;&lt;/encode-this&gt;" | littlego xmlcoder

    tail == tail STDIN to a UI window
           Pass the -h option to get detailed help.

         ex: $ littlego tail -h
             $ program_with_lots_of_output | littlego tail

       </pre>
    </dd>
</dl>
<p>
The Linux/Unix developer working in the Mac terminal should
take a quick lock at the man pages for these Mac
command-line tools: 
       launchctl, oascript, open, pbcopy, xsltproc.
</p>

</di:section>
<di:section id="sec_next" topic="What's Next">

<p>
For the next release we will try to get to some of the
following.
</p>

<ul>
<li> GUICE IOC integration </li>
<li> Application user may run that edits the user java.policy file
       to grant trust to jar files loaded from frickjack.com. </li>
<li> Migrate to Mercurial SCM system </li>
<li> Add Locale to littleware Session, and extend i18n, l10n support </li>
<li> Extend use of .properties files in UI configuration </li>
<li> Rework Applet code to take advantage of static 
           member data persistence </li>
<li> Migrate to EJB3.0 appserver. </li>
<li> Rework JSimpleAssetView SWING component </li>
<li> Introduce ACL-editor Swing Applet </li>
<li> Add SOAP and REST bindings </li>
<li> Add a new acl-check JSP tag, and an access-denied error-page. 
          </li>
<li> Rework security-related Exception hierarchy </li>
<li> Begin work on backend MySQL support </li>
<li> Begin work on backend Oracle support </li>
</ul>
               
</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
