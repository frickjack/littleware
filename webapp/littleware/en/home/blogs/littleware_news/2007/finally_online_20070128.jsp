<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<%@ page contentType="text/xml;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:useBean id="lw_defaults" class="littleware.web.beans.DefaultsBean" />
<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
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
    <title>Littleware finally online</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/finally_online_20070128.jsp"/>
    <id>urn:uuid:69FC9CC9-1D23-4885-BD49-FC97A8BBE34D</id>
    <updated>2007-01-30T18:31:02Z</updated>
    <summary type="xhtml">Littleware is online. frick.
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Finally Online"
      last_modified="02/08/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="Getting Online">
<di:timestamp year="2007" month="01" day="28" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro>
<p>
It&apos;s the end of February, 2007, and Littleware
is finally online - even if only in a limited way.  
This blog entry describes the process I went through
to get this site onto the Internet.
I&apos;m not an expert in doing this kind of thing,
so let me know if you spot any mistakes.
</p>
</di:intro>
<di:section topic="Plugin Publishing">
<p>
Times are good for web content providers.
There are a variety of free or inexpensive
ways for a person with something to say to publish to the internet.
An easy though somewhat inflexible
way to publish is to simply subscribe to
one of the many free <e>plugin</e> services.
Some of the most successful online companies
over the last five years offer free publishing
services which allow a content provider (you and me)
to easily submit and manage media that <e>plugs into</e>
the provider website.
The service provider makes money by selling the advertising
that the provider decorates its site with.
Examples of this kind of plugin service include blogging 
    (like <a href="http://www.blogspot.com">Google blogs</a>), 
social networking (like <a href="http://www.myspace.com">MySpace</a>),
online marketplace (like <a href="http://www.ebay.com">Ebay</a>),
and online media (like <a href="http://www.youtube.com">YouTube</a>).
</p>
</di:section>
<di:section topic="Hosted Publishing">
<p>
Another way for a content provider can publish online is to
acquire diskspace behind a hosted web server.
A hosting service sells space behind one of its servers
for a monthly fee.  A hosting service usually offers different
levels of service for different rates including database access and
CGI (server side execution of scripts) access.  
Some hosting providers also offer web design and online store management.
</p>
<p>
By renting hosting space the content provider gains not only the
flexibility to manage his on web site, but also the complexity that
goes along with site management.  Often small companies will
outsource their site management to a web site designer.
There are comercial packages to help with web design -
<a href="http://www.adobe.com/products/dreamweaver/">Adobe Dreamweaver</a>
is one of the most prominent.
</p>
<p>
A web site manager may also take advantage of the <em>web services</em>
that various web-based companies vend to help 
less sophisticated web designers build services into their sites.
<a href="http://www.paypal.com">PayPal</a> and 
<a href="https://www.google.com/adsense/">Google Adsense</a>
are two successful examples of these web services 
where customer web sites access data and functionality served out
by the web-service&apos;s servers.
The vague term <a href="http://en.wikipedia.org/wiki/Web2.0"> Web2.0 </a>
generally refers to this
web-application architecture where a web-service provider
exports service APIs that are then accessed by third party web sites,
standalone user applications 
(like <a href="http://earth.google.com"> Google Earth</a> 
   and <a href="http://www.apple.com/itunes/store/">iTunes</a>),
and by javascript based applications that run in web browsers.
</p>
</di:section>
<di:section topic="Colocation and Webapps">
<p>
A web content provider that wants to provide web2.0 services
from his site may subscribe to a hosting service that
allows him to plug into a preset database-backed
<a http="http://en.wikipedia.org/wiki/Application_Server">application-server </a>
framework 
(like <a href="http://en.wikipedia.org/wiki/LAMP_%28software_bundle%29"> PHP </a>,
  Microsoft&apos;s <a href="http://www.asp.net/"> ASP .NET </a>,
  <a href="http://www.rubyonrails.org/"> Ruby on Rails </a>,
  and <a href="http://java.sun.com/javaee/"> java enterprise edition </a>).
<gen:expand>
<gen:summary>
An application server is a 
<a href="http://en.wikipedia.org/wiki/Software_framework">software framework</a>
that fascilitates the development of database driven 
<a href="http://en.wikipedia.org/wiki/Web_application">web applications (webapp)</a>.
</gen:summary>
<gen:description>
A typical webapp-driven web site dynamically generates a large portion
of its content via
<a href="http://en.wikipedia.org/wiki/Database">database</a> driven
programs that run as plugins within the the application server
(web2.0 services are a kind of web application).
Most frameworks also encourage a 
<a href="http://en.wikipedia.org/wiki/Model_view_controller">
    model-view-controller (MVC)</a>
design for the webapps they support.  The MVC
design allows for separation of implementation roles so
that database designers focus on the data model,
programmers implement the webapp controls, and
web designers handle the visual presentation.
A large scale web site also requires full-time effort
from staff filling other 
<a href="http://en.wikipedia.org/wiki/Unified_Software_Development_Process">development process</a> rolls including project management, 
testing and quality assurance, and different
kinds of architect and design (software, visual, navigation, etc.).
</gen:description>
</gen:expand>
</p>
<p>
A developer building a large scale or specialized site
may prefer to have complete control over the installation and
configuration of the various components 
(database, web, and application servers, networking, backup) 
that coordinate under the hood of a webapp.
A <a href="http://en.wikipedia.org/wiki/Colocation">colocation</a>
provider allows a webapp developer complete control over a machine
(usually purchased by the webapp designer) housed in a colocation
<a href="http://en.wikipedia.org/wiki/Datacenter">datacenter</a>.
In addition to providing a safe home for the webapp server
with conditioned power and reliable network access at a specified bandwidth,
a collocation provider will often also provide
support services like domain registration, DNS, e-mail server access,
and server backup.
For low traffic web-sites, colocation is usually only necessary if
the web developer is building a site intended to serve out
webapps (as opposed to static content),
and the developer would like complete control over the
server software configuration.
</p>
</di:section>
<di:section topic="Littleware - cardboard box data center">
<p>
Here at Littleware we find ourselves in the unique (?) position
where colocation is our ideal solution (since we are building
webapps in a custom software environment), 
but we do not want to buy a new server or
pay the monthly fee that colocation would require
until we actually make our first dollar.
We have therefore come up with our own custom 
<i>cardboard box datacenter</i> 
- or <em>datacenter on a bookshelf</em>.
- with a nod to Sun Microsystem&apos;s
<a href="http://www.sun.com/blackbox/">blackbox</a> project.
</p>
<p>
The cardboard box system solves the classic datacenter problems
with the novel solutions.
<dl>
<dt> computer processing </dt>
      <dd> The littleware web site runs on a
             Mac Powerbook G4 whose monitor no longer works,
             but is otherwise still running (for now) 
             on a bookshelf in my apartment.
             The Powerbook serves double duty running the servers
             for the Littleware web site (postgres database, 
             Tomcat java, Apache static web) and acting as
             the wireless base station for our little apartment
             intranet (the littleware dev laptop is the only
             other machine on the network though).
             I&apos;ll blog about the current Littleware software deployment
             and it&apos;s ability to scale to different configurations
             in the near future.
      </dd>
<dt> electric power conditioning and redundancy </dt>
       <dd> power strip, and the Powerbook has a battery backup in it </dd>
<dt> cooling </dt>
       <dd> window in the room, central air on hot days in Auburn, AL </dd>
<dt> networking and internet access </dt>
       <dd> <a href="http://www.bellsouth.com/dsl">BellSouth Fast Access Xtreme! </a>
           384Kbs upstream and a static IP address for $38-/month.
           It turns out that the BellSouth supplied DSL modem
           has some pretty nifty networking capabilities built in -
           DHCP, NAT, and the ability to specify a specific machine
           to receive all incoming traffic to our static IP address.
          </dd>
<dt> DNS and domain registration </dt>
       <dd> 
     Registered littleware.frickjack.com with
     <a href="http://godaddy.com"> GoDaddy </a>, and just
     use the free GoDaddy DNS service to register our IP address.
       </dd>
<dt> Backup </dt>
      <dd>
       Nightly tar-gz backup to firewire attached iPod.
       I don&apos;t have nearly enought music or podcasts to fill
       my iPod, so I just drop my backups there.
       I&apos;ll blog about our backup process,
       and publish our backup scripts for download in the near future.
      </dd>
</dl> 
</p>
<p>
When I first started looking into setting up the cardboard datacenter
it took me a while to figure out how to get properly registered with DNS.
I knew I could buy a static-IP address from bellsouth, 
and I knew that I could buy a DNS domain
name from a registrar, but I did not know who was a good registrar
or how much the domain name would cost.  After googling around a bit,
I came across the <a href="http://godaddy.com">GoDaddy</a> registrar.
I remembered GoDaddy as
the company responsible for the mystifying super bowl comercials
with the hot girls and screaming nonsense, but their comercial
did not make it clear to me what the company
actually does.  Anyway - it turns out GoDaddy is a DNS registrar
and hosting company, and they sell a one year lease on a domain name
for under $10-.  
</p>
<p>
Now I knew how to get an IP address and a domain name,
but it took me a while to figure out how to connect the two in
DNS, because a lot of GoDaddy&apos;s documentation assumes that
their customer is subscribing to a hosting service that 
manages DNS.  We could run a DNS server on our Powerbook,
but that is a whole can of worms, and GoDaddy requires
that a domain owner specify two DNS servers for redundancy anyway.
After a few hours more reading I finally figured out that
GoDaddy allows its customers to use the GoDaddy hosted DNS servers.
</p>
<p>
With DNS figured out, the Littleware IT department is ready to
get our Powermac connected to the internet at 
<a href="http://littleware.frickjack.com">http://littleware.frickjack.com</a>.
Unfortunately the littleware.com domain name was already 
claimed by a do-nothing site, so we went ahead with frickjack.com
with a littleware.frickjack.com CNAME.
The GoDaddy web-based domain management tools made it easy to
update their DNS servers with our static-IP for the frickjack.com A-record,
and add the littleware CNAME.  I was surprised by the CNAME functionality -
I thought I would have to buy subdomain names from the registry.
</p>
<p>
While waiting to figure out our domain mapping, we have gone
ahead and souped up the ipfw firewall rules on our Powermac cardboard server.
MacOS 10.4 client comes with a simple firewall GUI as part of 
System\ Preferences.app that allows us to control access to ports
across all network interfaces, but we would like more flexibility
in configuring our cardboard server.  
We want this flexibility because in addition to serving out the
littleware web site via its ethernet interface, our cardboard server
also acts as a wireless network gateway (DNS and NAT), file server 
(for backup and CVS repository), and print server for
our little apartment intranet.  In order to improve the security of
this central server, we want to add rules to the ipfw firewall that
only allow port 80 to be accessed from clients connecting via the ethernet
interface (which connects to the DSL modem), but allow connections to
print services, file sharing, and network services via the 801.11b wireless card
interface that our intranet accesses as a gateway.
Since the MacOS System\ Preferences.app does not allow per-inerface firewall
configuration, we resorted to setting up our own custom ipfw rules registered
at system startup time via a /Library/StartupItems script.
The System\ Preferences.app ipfw rules are (mostly) registered as ipfw rules 2000 
and higher, so we can easily insert several of our own custom rules below 2000.
Here is our ipfw startup script - let us know if you can recommend a better set
of ipfw rules: <br />
<code>
<pre>
<![CDATA[
$ ls -l /Library/StartupItems/Firewall/Firewall; cat /Library/StartupItems/Firewall/Firewall 
-rwxr-xr-x   1 root  wheel  750 Feb  8 18:12 /Library/StartupItems/Firewall/Firewall*
#!/bin/sh

##
# ipfw customization
##

. /etc/rc.common

StartService ()
{
    echo "Adding custom ipfw rules"
    # Send traffic to me via en0 to line 1500 for special handling
    /sbin/ipfw add 1000 set 1 skipto 1500 tcp from any to me in via en0
    /sbin/ipfw add 1020 set 1 skipto 2000 all from any to any
    # Only allow external traffic to port 80
    /sbin/ipfw add 1500 set 2 allow tcp from any to any established
    /sbin/ipfw add 1510 set 2 allow tcp from any to me dst-port 80 in
    /sbin/ipfw add 1520 set 2 deny ip from any to any # default rule
}

StopService ()
{
    echo "Removing custom ipfw rules"
    /sbin/ipfw delete set 2
    /sbin/ipfw delete set 1
}

RestartService ()
{
    echo "Restart is a NOOP"
}

RunService "$1"

]]>
</pre>
</code>
</p>
<p>
I&apos;ll blog in the near future about the software
(postgres, tomcat, apache)
we need to configure on the Mac cardboard server to get the Littleware webapp online.
</p>

</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
