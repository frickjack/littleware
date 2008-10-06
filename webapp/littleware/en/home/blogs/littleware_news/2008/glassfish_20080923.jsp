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
    <title>Software, Software, Software</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2008/glassfish_20080923.jsp"/>
    <id>urn:uuid:9dfa4d55-d115-4916-acf5-eca378674995</id>
    <updated>2008-09-28T18:31:02Z</updated>
    <summary type="xhtml">Great software tools I've picked up over the last several months.</summary>
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

<di:entry author="Reuben" title="Great Software Tools">
<di:timestamp year="2008" month="09" day="28" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro>
<p>

</p>
</di:intro>
<di:section topic="Software, Software, Software">
<p>
    The <a href="http://frickjack.com">frickjack.com</a> site
    now runs on a <a href="http://glassfish.java.net">Glassfish</a>
    java application server.  I switched most of my development
    to target glassfish rather than the <a href="http://tomcat.apache.org">Tomcat</a>
    application server several months ago, but only
    switched frickjack.com over in the last month.
The main reason I prefer glassfish over tomcat is that glassfish
implements a complete java enterprise software stack
including EJB and JSF while Tomcat only implements a subset.
</p>
<p>
    We've been using the <a href="http://selenic.com/mercurial">Mercurial</a>
source control management (SCM) software to manage the littleware codebase
for the last several months, and that has also worked out great.
I like the flexibility that a distributed
SCM encourages better than the tagging and branching
discipline required with a central repository system like SVN.
</p>
<p> <a href="http://netbeans.org">Netbeans</a> has been my
IDE for most java and java webapp development over the
last several months, and that has worked out great.
Netbeans works great on Windows - which I've been spending a lot 
of time on lately, and the editor's integration with the java
compiler is great.
</p>
<p> A lot of my recent work is web based, so I've had
the chance to improve my understanding of
CSS and javascript.  I now use the 
<a href="//code.yahoo.com/yui/">Yahoo User Interface</a> (YUI)
library as much as possible.  The YUI has a
great set of cross browser CSS and javascript tools,
and has been well worth the small amount of time 
it took to learn at a basic level.
</p>
<p> The <a href="http://code.google.com/guice/">Guice</a>
java dependency injection library is another tool that
has improved my development process.
I've been coding against interfaces for a while now,
but the homebrew mechanisms I developed to manage
interface to implementation mapping at application
bootstrap time were clumsy at best.  Guice is
a significant improvement.
</p>
<p> I also use
<a href="http://scala-lang.org">Scala</a> in some of my
latest java projects.  I enjoy
the language.  It took me a while to get back into
the swing of functional programming, and I still
learn more all the time, but I already appreciate many of
scala's features like first type functions, closures,
type inference, pattern matching, and mixins.
The C# language also has several of these features,
but scala leverages them more effectively
throughout its design - especially in
the scala collections framework.
</p>
<p>
    I still write many lines of code in vanilla java.
I've become more comfortable with MVC patterns
writing standalone graphical UI's in both java and C#.
I recently developed a little Swing component that uses
the java2dAPI to render measurement data over a background image.
The java2d APIs are great, and it was easy to 
get the component working in an applet that dynamically accesses real time
data from a server.
</p>
<p>
Finally, my work at the library has given me the chance to 
write several applications in C# .NET.  I've enjoyed
working with C#, but it is clearly derivative of java.
I spend a lot of time running Windows (XP at work,
Vista at home) now.  I prefer the Mac environment - I love the
Unix shell and command line tool set, but Windows is
what we use at the library, and most contract clients are on
Windows too.  In fact the Windows powershell provides a 
good command line, and all the important tools I rely
on (Mercurial, emacs, wget, etc.) are available on Windows.
</p>
<p>
My work at the library is also often done via X-remote
session with an Linux server.  Linux is a great
development and server environment, and 
I spend a lot of time testing open source 
library software and servers developed on Linux, 
and customizing that code (like the <a href="http://d-space.org">DSpace</a>
institutional repository system) to address some
specific task or problem in our environment.
</p>
<p>
Most of my software development work targets a java technology stack.
Over the next few months I hope to take advantage of java web-start 
technology to manage the deployment of some applications at the
library.  I expect that will be a good experience, and I'll
have another set of tools that cary over for use with littleware.
</p>

</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
