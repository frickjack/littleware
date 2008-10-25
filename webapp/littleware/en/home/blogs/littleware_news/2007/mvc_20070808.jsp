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
    <title>MVC and Swing experience</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/mvc_20070808.jsp"/>
    <id>urn:uuid:8D0BB613-7FBF-4E3E-BE41-F1FD696F86F7</id>
    <updated>2007-08-08T18:31:02Z</updated>
    <summary type="xhtml">Some of my experiences using MVC and Swing.
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

<di:entry author="Reuben" title="MVC and Swing">
<di:timestamp year="2007" month="08" day="08" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro>
<p>
I have been working with Java Swing the last
several weeks developing UI controls for
interacting with the littleware asset repository.
In the process of doing this work I've developed
some ideas on implementing MVC (model, view, controller) 
when building UI components with Swing.
</p>
</di:intro>
<di:section topic="Component Design">
<p>
One approach to building a UI tool like an AssetBrowser (for example)
is to build up the UI widgets (buttons, text areas, whatever),
associate list, table, and tree views with corresponding data models,
and wire up event-handler controls to catch UI action events.
This design allows me to build up an application whose implementation
separates model, view, and control,
but another developer may not be able to easily use
the code I've written to write another application that
is similar in some ways.
</p>
<p>
Rather than simply build a series of standalone UI applications;
I want to build a library of Swing UI components that another
developer may use to assemble his own applications.
Furthermore, I don't want to simply deliver a canned layout
of Swing components where a developer dives in and wires
up all the constituent Swing components (buttons, whatever).
I want each littleware UI component to be a standalone balck-box JComponent
or JPanel subtype that a developer may easily add into
his own application layout; our blackbox has its own datamodel
that a controller may manipulate to change the view, a view that
is configurable as a bean and fires its own set of events
that an application builder can register listeners to for
implementing a custom control, and a set of canned controls
that implement some generic behaviors.
</p>
<p>
For example, the littleware.apps.swingclient.JAssetBrowser
renders a view of its littleware.apps.swingclient.AssetModel,
and provides UI controls by which users may request to 
navigate the browser to view a different asset.
When the user hits a link to another asset, the JAssetBrowser
fires a littleware.apps.swingclient.event.NavRequestEvent that
a user supplied controller may respond to to either change
the view or carry out some other action.
The littleware.apps.swingclient.controllers.SimpleAssetViewController
is a LittleEvent listener that we can register with the JAssetBrowser
to implement the expected navigation behavior.
We can also register the controller with a JSimpleAssetToolbar
that may provides more navigation controls to the UI.
</p>
<p>
Introducing our own littleware.apps.swingclient.LittleEvent
event types and LittleListener event listeners allows 
other developers to easily incorporate our Swing components
into custom applications with custom behaviors while
maintaining our flexibility to new control implementations
that upgrade the control's internal 
implementation and subcomponent layout.
The disadvantage of adding our LittleEvent/LittleListener
layer is that the developer using our components does not
have detailed access to the inner workings of the component.
</p>

</di:section>

<di:section topic="Mac Envy">
<p>
In assembling the simple controls I've managed to code so far,
I've developed more and more respect for the Macintosh UI 
designers.  Applications on the Mac are not only built with
sophisticated UI components from the Cocoa libraries, but
also include sophisticated custom widgets with their own
data models and rendering.  For example, the iChat text-entry widget
automatically expands to fit text pasted into it.
The Addressbook display/edit widget provides a view of an address card
which implements its own selection mechanism, popup menus, and
edit-mode.  The iLife suite of tools are orders of magnitude more
sophisticated that the generic IT-type applications (iChat, 
iCal, AddressBook) that I try to imitate. 
</p>
<p>
Working with Swing I also often find myself missing the ability
in web development to easily intermix formatted text, images,
and control widgets on a scrollable canvas.  
With Swing I often find myself assembling a view of widgets
intermixing labels, buttons, textfields, trees, whatever; and I
wind up with something that does not fit nicely onto the screen,
does not allow simple expand/collapse type operations of subpanels, and
is not scrollable within a JScrollPane.  
</p>
<p>
I'm sure that part of my problem is just inexperience with 
GUI design in general and Swing in particular.
I've only been actively working with Swing for 6 months or so.
I have experimented with
dynamically adding/removing components from panels
with some success,
but I haven't yet convinced myself that what I'm doing isn't
the equivalent of driving a car in reverse on the freeway -
it gets you where you want to go, but it's not the right thing to do.
I plan to experiment with some of the Swing text components
to see how hard it is to wire up controls that add/hide data
in the text display in response to clicking on icons/whatever
in the text.  Finally, I'm looking into Java2D, but I want to avoid
writing too many of my own rendering pipelines.
</p>
<p>
Overall I'm happy developing with Swing.
The framework has good MVC support, and a nice set of standard
widgets, and there are more widgets available from open source
projects online.  Compared to perl/Tk, python Tkthing,
and Motif - the framework is great, and I think it can
sit side by side with Qt ok too.  I've never worked with
Microsoft's or Apple's main GUI frameworks, but I suspect that
they provide a richer set of widgets and graphical layout.
I would like to have a graphical layout
tool rather than fighting with LayoutManager logic,
but I suspect I can get that when I make the move to using a good
IDE like NetBeans.
We'll see how it goes.
</p>

</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
