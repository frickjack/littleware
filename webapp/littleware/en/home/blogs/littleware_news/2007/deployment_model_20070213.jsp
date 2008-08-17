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
    <title>Littleware server deployment</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/deployment_model_20070213.jsp"/>
    <id>urn:uuid:1E925B8F-353C-4153-B1CC-7D306F64EEBC</id>
    <updated>2007-02-13T18:31:02Z</updated>
    <summary type="xhtml">Littleware server deployment overview.
    </summary>
  </entry>

</gen:websupport>
<gen:document 
      title="Server deployment"
      last_modified="02/13/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:di="http://www.littleware.com/xml/namespace/2006/diary"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<di:diary owner="Reuben">

<di:entry author="Reuben" title="Littleware Server Deployment">
<di:timestamp year="2007" month="02" day="13" time24="23:15" zone="central" />
<di:location city="Auburn" state="AL" country="USA" />
<di:journal>
<di:intro>
<p>
We introduced in an <a href="finally_online_20070128.jsp">earlier bLog entry</a>
the notion of a web-services provider and the littleware cardboard-box data center.
This blog entry describes the roles of the different software servers
that make up the current littleware deployment, and the ways
those servers may be deployed to different machines to scale
a littleware server install to accommodate different workloads. 
I&apos;m not an expert in doing this kind of thing,
so let me know if you have any suggestions.
</p>
</di:intro>

<di:section topic="Mutltier Architecture">

<p>
Littleware has a 
  <a href="http://en.wikipedia.org/wiki/Multitier_architecture">multitier</a>
software stack architecture illustrated below.
</p>
<dl>
  <dt><b>Level 1 - data tier</b></dt>
       <dd>
       <p>
       Littleware stores all its data (assets) in the file system and
     relational database system (RDMS) that make up the data tier.  
     Littleware stores asset metadata and search indexes
     in the database, and littleware stores asset
     bulk data blocks (XML, images, generic files)
     in a directory assigned to the asset on the filesystem.
        </p>
        <p> 
         The data tier may be hosted on one or multiple machines depending on 
      the type of deployment.  
      <a href="http://en.wikipedia.org/wiki/Network-attached_storage">
          Network attached storage (NAS)
       </a> or a <a href="http://en.wikipedia.org/wiki/Storage_area_network">
       storage area network (SAN) </a> may provide bulk-data filesystem.
       Some database systems (like <a href="http://www.oracle.com">Oracle</a>
       and <a href="http://www.ibm.com/db2">DB2</a>)
       support clustering where multiple machines serve out a single database.
       </p>
       <p>
       For the <a href="finally_online_20070128.jsp">cardboard box</a>
      deployment backing littleware.frickjack.com, we run a 
      <a href="http://postgresql.org">Postgres</a> database on our
      single server, and use the local disk for filesystem storage.
        </p>
       </dd>

  <dt><b> Level 2 - API middleware tier </b></dt>
  <dd>
      <p>
     The middleware tier exports network-accessible APIs by which
   user-application clients access and manipulate asset data
   stored in the data tier.  The littleware middleware code (written in java)
   that runs at this level enforces asset semantics
   (we will bLog about the littleware asset workflow in the near future), 
   provides security (<a href="http://java.sun.com/products/jaas/">JAAS</a> based
   authentication and access control),
   and provides transparent cacheing of asset metadata pulled from
   the data tier&apos;s database.
      </p>
      <p>
   The littleware middleware code is written in java,
   and hosted by one or more independent
      <a href="http://java.sun.com/javaee/">enterprise java</a> 
   application servers from which the code exports 
   <a href="http://java.sun.com/rmi/">RMI</a> and (eventually)
   <a href="http://en.wikipedia.org/wiki/SOAP">SOAP</a> and 
   XML based APIs.
   The littleware.frickjack.com deployment runs
    within a single <a href="http:tomcat.apache.org">Tomcat</a>
   application server on the cardboard server machine.
       </p>
  </dd>
  <dt><b> Level 3 - Application tier </b></dt>
      <dd>
       <p> 
       The level-2 APIs provide generic functions for manipulating different
      types of assets in flexible ways.  The application tier builds on
      these APIs to implement application-level APIs and web-based user interfaces
      with specific workflow constraints.  For example, the littleware.frickjack.com
      website implements a user-registration function that makes several 
      calls to the level-2 APIs to create a new user asset that is automatically
      added to a &quot;web-registered&quot; user group which 
      is granted wider access to the website via access control lists.
      </p>
       <p>
       A developer may build a traditional standalone application
     at the application tier (we intend to build some littleware administration tools
     in this way), but we imagine that many applications will
     run within an application server framework (javaee, .NET, ruby-on-rails, whatever)
     running on one or more machines to provide web-based services like littleware.frickjack.com.
       </p>
      <p>
     The littleware.frickjack.com application cardboard deployment runs within the same
     Tomcat instance as the level-2 code, so our application is able to access
     the level-2 APIs via direct procedure call rather than pay the added overhead
     of accessing the APIs via RMI.   
       </p>
      </dd>
<dt><b> Level 4 -Network switch tier </b></dt>
     <dd>
      <p>
     A <a href="http://en.wikipedia.org/wiki/Layer_7_switch">Layer 4-7 (application layer)
    network switch</a>
    flexibly accepts network requests routed to a single IP address and redirects
    those requests to different application servers based on the URL, HTTP-session, or port that
    the request is accessing.  These smart switches also act as a firewall.
    Network switches like 
    <a href="http://www.netscaler.com"> NetScaler</a> and 
    <a href="http://www.bigip.com">BigIP F5</a> allow a web application
    deployment to transparently 
    <a href="http://en.wikipedia.org/wiki/Scale_out">scale out horizontally </a>
    by adding additional servers.
    The littleware software stack is designed so that database load at level-1
    is limited by multiple-server aware
    cacheing at level-2 as the deployment scales, and level-3 web applications
    scale out safely by routing different user sessions to different servers.
       </p>
      <p>
     Our cardboard-box deployment only has a single machine, so we
    simply run an <a href="http://www.apache.org">Apache</a> server
    on port 80 that uses 
    <a href="http://tomcat.apache.org/tomcat-3.3-doc/mod_jk-howto.html">mod_jk</a>
    to route URLs with non-static content
    to the Tomcat level-3 server.
    We rely upon the MacOS-X ipfw firewall to restrict external access
    to port 80 on our cardboard-box server.
      </p>
    </dd>
<dt> <b>Level 5 - User tools </b></dt>
    <dd>
      <p>
    Level-5 simply refers to the tools that the end-user works with
   to do whatever he needs to do.
   A tool may simply be a web-browser accessing an application-level
   web server, but a tool may also be a script or full blown standalone
   application that accesses level-2 and level-3 APIs to accomplish
   whatever task the tool is designed to support.
   We&apos;ll go into more detail on the kinds of littleware user-tools under development
   in future bLogs.
      </p>
    </dd>
</dl>

<lw:filter begin="<svg" end="</svg>">
     <c:import url="deployment_model.svg" />
</lw:filter>

</di:section>
<di:section topic="Cardboard Box Configuration">
<p>
At the end of the day, the littleware.frickjack.com cardboard-box
deployment (old Powerbook running MacOS-10.4) runs a postgres database,
Tomcat JSP/servlet container, and an Apache web server
to support the littleware software stack.  I won&apos;t go into
detail on the installation and configuration of these packages -
since each includes its own documentation.  However, here
are some notes that may be helpful.  I&apos;ll add to this list
as things come up.
</p>
<ul>
<li> <gen:expand><gen:summary>
     MacOS-10.4 comes with Apache 1.3 already installed and ready to run
     with startup scripts and everything - we just need to enable <i>Web Sharing</i>
     within the System Preferences app to start the server.
     We customized our littleware.frickjack.com Apache install by
     setting the server name, and adding /etc/httpd/users/tomcat.conf (listed below)
     that configures mod_jk to route several URLs to our Tomcat java server.
     </gen:summary>
     <gen:description>
     <code>
      <pre class="code">

$ cat /etc/httpd/users/tomcat.conf 
LoadModule jk_module           libexec/httpd/mod_jk.so
AddModule mod_jk.c

JkWorkersFile /etc/httpd/jk/workers.properties 
JkLogFile /var/log/httpd/mod_jk.log
JkLogLevel info
JkLogStampFormat "[%a %b %d %H:%M:%S %Y] "
JkMount /littleware/en/* littleware
JkMount /littleware/fre/* littleware
JkMount /littleware/spa/* littleware
JkMount /littleware/zh/* littleware
JkMount /littleware/webdav/* littleware
JkMount /littleware/lib/xsl/* littleware

$ cat /etc/httpd/jk/workers.properties
workers.tomcat_home=/Library/Tomcat/Current
workers.java_home=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home

worker.list=littleware

#
# Definition for Ajp13 worker
#   Note: we have configured Tomcat on a non-standard port
#
worker.littleware.port=9009
worker.littleware.host=127.0.0.1
worker.littleware.type=ajp13

</pre>
</code>
     </gen:description> 
     </gen:expand>
        </li>

<!-- ............................................................... -->

<li> <gen:expand>
     <gen:summary>
   We have a link-tree setup under /Library/Tomcat to version
  our Tomcat install, and we have a 
  <a href="http://developer.apple.com/macosx/launchd.html">launchd</a>
  script to manage the server startup at boot time.
  This is MacOS specific, but Linux (init.d) and Windows (services) based servers
  will need something similar.
     </gen:summary>
     <gen:description>
<code>
<pre class="code">
<![CDATA[
$ ls -l /Library/Tomcat/
total 32
drwxr-xr-x   6 root  admin  204 Jan 21 17:45 Versions/
lrwxr-xr-x   1 root  admin   20 Oct 26 13:04 bin@ -> Versions/Current/bin
lrwxr-xr-x   1 root  admin   21 Oct 26 13:04 conf@ -> Versions/Current/conf
lrwxr-xr-x   1 root  admin   21 Oct 26 13:05 logs@ -> Versions/Current/logs
lrwxr-xr-x   1 root  admin   24 Oct 26 13:05 webapps@ -> Versions/Current/webapps

$ ls -l /Library/Tomcat/Versions
total 8
drwxrwxr-x   16 tomcat  tomcat  544 Nov 28  2003 4.1/
lrwxr-xr-x    1 root    admin    19 Jan 21 17:45 Current@ -> apache-tomcat-6.0.7
drwxrwxr-x   20 tomcat  tomcat  680 Jan 21 17:44 apache-tomcat-5.5.20/
drwxrwxr-x   18 tomcat  tomcat  612 Feb 11 15:01 apache-tomcat-6.0.7/

$ cat /Library/LaunchDaemons/com.littleware.tomcat.plist 
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
        <key>Disabled</key>
        <false/>
        <key>GroupName</key>
        <string>tomcat</string>
        <key>Label</key>
        <string>com.littleware.tomcat</string>
        <key>OnDemand</key>
        <false/>
        <key>Program</key>
        <string>/Library/Tomcat/bin/catalina.sh</string>
        <key>ProgramArguments</key>
        <array>
                <string>/Library/Tomcat/bin/catalina.sh</string>
                <string>run</string>
                <string>-security</string>
        </array>
        <key>RunAtLoad</key>
        <true/>
        <key>ServiceDescription</key>
        <string>Littleware Tomcat server </string>
        <key>UserName</key>
        <string>tomcat</string>
        <key>SessionCreate</key>
        <true/>
        <key>StandardErrorPath</key>
        <string>/dev/null</string>
        <key>StandardOutPath</key>
        <string>/dev/null</string>
</dict>
</plist>

]]>
   </pre>
   </code>
     </gen:description>
   </gen:expand>
</li>
<li><gen:expand>
    <gen:summary>
    Although we installed Postgres under its standard /usr/local/pgsql directory,
   we configured the database to reside under /Library/Postgres.
   We wrote a /Library/StartupItems/Postgres/Postgres script to launch
   Postgres with its standar output piped through <i>rotatelogs</i> -
   I could not quickly figure out how to accomplish something similar with launchd
   without writing a wrapper script.
   Finally, we added a /etc/sysctl.conf file for MacOS-X to read at boottime
   that increases the shared-memory limits for the machine, so
   we could configure Postgres to use a larger chunk of memory.
    </gen:summary>
    <gen:description>
<code>
<pre class="code">
<![CDATA[
$ cat /etc/sysctl.conf 
#
# Setup 100 MB shared memory segment
# 2007/01/25 - Reuben Pasquini
#
kern.sysv.shmmax=104857600
kern.sysv.shmmin=1
kern.sysv.shmmni=32
kern.sysv.shmseg=8
kern.sysv.shmall=25600

$ cat /Library/StartupItems/Postgres/Postgres
#!/bin/sh

##
# Postgres DB Server - running under mysql user since it is already there
##

. /etc/rc.common

StartService ()
{
    # Took out if [] test on /etc/hostconfig - since it was causing XML/JSP problems
    # in our blog display - frick!
        echo "Starting Postgres DB server"
        su -fm mysql -c '/usr/local/pgsql/bin/pg_ctl start -D /Library/Postgres/data 2>&1 | /usr/sbin/rotatelogs /Library/Logs/Postgres/postgres_%F_%T  36000 -360 &'
}

StopService ()
{
    echo "Stopping Postgrs DB server"
    su -fm mysql -c '/usr/local/pgsql/bin/pg_ctl stop -m fast -D /Library/Postgres/data'
}

RunService "$1"
]]>
</pre>
</code>
    </gen:description>
    </gen:expand>
</li>

</ul>

</di:section>


</di:journal>
</di:entry>

</di:diary>
</gen:document>

</gen:view>
