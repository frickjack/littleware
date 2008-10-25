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
    <title>Littleware Mac FAQ</title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="http://${lw_defaults.defaults['serverName']}/littleware/en/home/blogs/littleware_news/2007/macfaq.jsp"/>
    <id>urn:uuid:A0773B71-FF78-45F7-92F9-4C0D57ECC08E</id>
    <updated>2007-08-08T18:31:02Z</updated>
    <summary type="xhtml">Mac FAQ - just a few notes on working
       with the macintosh.
    </summary>
  </entry>

</gen:websupport>

<gen:document 
      title="Macintosh Tips"
      last_modified="02/16/2007"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns="http://www.w3.org/1999/xhtml"
     >

<gen:description>

<h2> <a href="http://www.apple.com" type="external"> Macintosh </a> Stuff </h2>

<table>
<tr>
<td>
<img src="/littleware/lib/img/macosxlogo.gif" />
</td>
<td>
<p>
I have had great experience developing on the Mac
since I switched to the Mac as my 
primary machine soon after the release
of MacOS-10.1.
Here is some miscelaneous macintosh info that I will
try to add to from time to time.
Be sure to checkout the 
Apple <a href="http://developer.apple.com">developer</a> 
site for lots of great information on writing code on MacOS.
</p>
</td>
</tr>
</table>

</gen:description>

<gen:faqlist>
   <addr:contact_info>
    <addr:email> pasquinir@bellsouth.net </addr:email>
  </addr:contact_info>

<gen:faq last_updated="02/16/2007">
    <gen:summary>
     Postfix setup to get sendmail and mail command-lines to work
    </gen:summary>
    <gen:description>
   <p>
    MacOS comes with the <a href="http://www.postfix.org">Postfix</a> 
    system all installed and almost ready to send 
    <a href="http://en.wikipedia.org/wiki/SMTP">SMTP</a> mail.
    One of the nice things to be able to do is shell out from a script,
    and invoke <i>sendmail</i> to send out an e-mail,
    but I found that e-mail sent this way to my ISP mail account would bounce back at me,
    because Postfix gave the receiving mail server a sender-hostname
    that is only valid on my local intranet.
    </p>
    <p>
    Now that I have my own static-IP and domain name 
    ( <a href="http://littleware.frickjack.com">frickjack.com</a>), I was able to fix
    this problem by simply setting the 'myhostname' and 'mydomain' variables in 
    /etc/postfix/master.cf.  You can probably work around this without
    a static-ip by just using your gateway&apos;s IP address, but I 
    have not tried that.
    </p>
    <p>
     Anyway, now that Postfix specifies a valid source, we also need
     to configure it to relay all outgoing e-mail through our ISPs SMTP server -
     since most destination SMTP servers 
     (like <a href="http://mail.yahoo.com">Yahoo</a>) do not accept connections
     from untrusted peers.  We just need to specify our ISP server as 
     a 'relayhost' in /etc/postfix, and we&apos;re good to go.
     </p>
     <pre class="code">
$ grep -e littleware -e bellsouth /etc/postfix/main.cf
myhostname = littleware.frickjack.com
relayhost = [mail.bellsouth.net]

$ sendmail -t catdogboy@yahoo.com
To: catdogboy@yahoo.com
Cc:
From: pasquinir@bellsouth.net
Subject: Hello from Postfix!

If this works, then that is cool!
.
     </pre>
     <br />
    </gen:description>
</gen:faq>
<gen:faq last_updated="02/16/2007">
    <gen:summary>
     setting up user and group disk quotas
    </gen:summary>
    <gen:description>
   <p>
    This <a href="http://sial.org/howto/osx/quota/">page</a> has a great overview
    on how to setup user and group disk quotas.
   </p>
    </gen:description>
</gen:faq>

<gen:faq last_updated="02/14/2007">
    <gen:summary>
     ssh remote login and VNC
    </gen:summary>
    <gen:description>
    <p>
    MacOS-10 comes with 
       <a href="http:/www.openssh.com">sshd</a>
     configured and ready to run. 
    To allow remote access to a machine via ssh and sftp,
    simply enable the 'Remote Login' checkbox in the
    System\ Preferences.app Sharing screen.
    While you&apos;re at it, set your computer-name
    in that same screen to something unique and interesting.
    With those two steps done, you can now access your machine
    from a remote terminal by running:
        <code>
        <pre class="code">
         $ ssh machine-name.local.
        </pre>
        </code>
    , where .local. is the DNS domain that the Mac Bonjour
    multicast-DNS service automagically sets up for us
    on our local network.
    </p>
    <p>
    My experience has been that running ssh and sftp can take a long time
    (three or four minutes) to finally connect to a remote machine
    that the client machine is accessing as a wireless router.
    I have not been able to figure out what ssh is doing - since
    lookupd and the dig DNS tool both quickly return the expected IP address.
     </p>
     <p>
     Simple ssh access to a remote machine is great for me when I just
     want to do a <i>cvs update</i> on a remote machine to release some new web
     pages, or <i>vi</i> a file on the remote machine,
     or if I need to run <i>apachectl</i>, <i>pg_ctl</i>, <i>launchctl</i>, 
     <i>reboot</i>, or something similar to restart a service,
     but sometimes I need full display access to the machine to run
     System\ Preferences.app or whatever.
     Take a look at 
         <a href="http://sourceforge.net/projects/cotvnc/">Chicken of the VNC</a>
     if you need full graphical display access to a machine from a remote client -
     it&apos;s free and easy to use.
     </p>
    </gen:description>
</gen:faq>

<gen:faq last_updated="02/14/2007">
    <gen:summary>
   Terminal background color.
   </gen:summary>
    <gen:description>
  Be sure to take advantage of the Terminal.app&apos;s background-color preference
  under the dialogue that pops up via the Terminal-&gt;Window_Settings menu item.
  I like to set different background colors for terminals doing different things
  (local shell, remote shell, editor, psql session, whatever).
  I also changed the default terminal background color so that it is different
  from the background color for TextEdit.app windows.
    </gen:description>
</gen:faq>

<gen:faq last_updated="02/14/2007">
    <gen:summary><a href="http://www.apple.com/macosx/features/ical/library/">iCal calendar library</a></gen:summary>
    <gen:description>
  Be sure to checkout <a href="http://www.apple.com/macosx/features/ical/library/">Apple's library of iCal calendars</a> at
      <a href="http://www.apple.com/macosx/features/ical/library/">http://www.apple.com/macosx/features/ical/library/</a> 
  to automatically add calendars like U.S. holidays to your iCal calendar.
    </gen:description>
</gen:faq>

<gen:faq last_updated="02/06/2007">
    <gen:summary>The <i>open</i> command-line</gen:summary>
    <gen:description>
    <p>
   The <i>open</i> command-line allows us to open a file or
application (.app) from the terminal as though we had double
clicked on the item in the Finder.
The command is useful in all kinds of situations.
For example, if you are browsing around via the terminal,
and you want to view an html or pdf document, then just run:
   <code> $ open bla.html </code>
or <code> $ open bla.pdf </code>,
and the documents will  pop open in your default web-browser
and PDF reader respectively.
If you just want to popup a directory in the finder:
   <code> $ open directory-path </code>
.  A handy shortcut to quickly mount a disk shared from another Mac
is: <code> $ open afp://remote-machine-name </code>.
Take a look at the man-page for more details.
   </p>
</gen:description>
</gen:faq>

<gen:faq last_updated="02/09/2007">
    <gen:summary>Customizing the ipfw firewall</gen:summary>
    <gen:description>
   <p>
   The following has been copied from the
   <a href="finally_online_20070128.jsp">finally online</a> bLog entry.
  </p>
   <hr />
<p>
MacOS 10.4 client comes with a simple firewall GUI as part of System\ Preferences.app that allows us to control access to portsacross all network interfaces, but we would like more flexibilityin configuring our cardboard server.  We want this flexibility because in addition to serving out thelittleware web site via its ethernet interface, our cardboard serveralso acts as a wireless network gateway (DNS and NAT), file server (for backup and CVS repository), and print server forour little apartment intranet.  In order to improve the security ofthis central server, we want to add rules to the ipfw firewall thatonly allow port 80 to be accessed from clients connecting via the ethernetinterface (which connects to the DSL modem), but allow connections toprint services, file sharing, and network services via the 801.11b wireless cardinterface that our intranet accesses as a gateway.Since the MacOS System\ Preferences.app does not allow per-inerface firewallconfiguration, we resorted to setting up our own custom ipfw rules registeredat system startup time via a /Library/StartupItems script.The System\ Preferences.app ipfw rules are (mostly) registered as ipfw rules 2000 and higher, so we can easily insert several of our own custom rules below 2000.Here is our ipfw startup script - let us know if you can recommend a better setof ipfw rules: <br /><code><pre>$ ls -l /Library/StartupItems/Firewall/Firewall; cat /Library/StartupItems/Firewall/Firewall -rwxr-xr-x   1 root  wheel  750 Feb  8 18:12 /Library/StartupItems/Firewall/Firewall*#!/bin/sh### ipfw customization##. /etc/rc.commonStartService (){    echo &quot;Adding custom ipfw rules&quot;    # Send traffic to me via en0 to line 1500 for special handling    /sbin/ipfw add 1000 set 1 skipto 1500 tcp from any to me in via en0    /sbin/ipfw add 1020 set 1 skipto 2000 all from any to any    # Only allow external traffic to port 80    /sbin/ipfw add 1500 set 2 allow tcp from any to any established    /sbin/ipfw add 1510 set 2 allow tcp from any to me dst-port 80 in    /sbin/ipfw add 1520 set 2 deny ip from any to any # default rule}StopService (){    echo &quot;Removing custom ipfw rules&quot;    /sbin/ipfw delete set 2    /sbin/ipfw delete set 1}RestartService (){    echo &quot;Restart is a NOOP&quot;}RunService "$1"</pre></code>
</p>
   </gen:description>
</gen:faq>
   

<gen:faq last_updated="10/09/2006"> 
    <gen:summary> pythonw and Mac python integration </gen:summary>
    <gen:description>
<p>
In addition to the standard python build (/usr/bin/python), the Mac
includes a customized pythonw binary that has hooks into the MacOS
event-passing system.  A script must run with pythonw
in order to use the Mac-specific python
modules (ex: Finder, CarbonDialog, ...).
</p>
<p>
I have written a couple little pythonw scripts that
you can copy.
<ul>
<li> finder_copy.py - a little script that uses the Finder to copy
         a file or directory to a destination directory.
         I like to use this script when moving large blocks of
         data, because the Finder has a nice progress bar that
         pops up for large copies.
    <code> 
    <pre>

$ cat ~/Library/bin/finder_copy 
#! /usr/bin/pythonw
&quot;&quot;&quot;
Copy the given source (file or directory)
under the given destination directory.

    finder_copy [-h] [-v] &lt;source&gt; &lt;destination&gt;

REPOSITORY:
    $Id: macfaq.jsp,v 1.6 2007/08/12 00:00:44 pasquini Exp $

&quot;&quot;&quot;

import sys
import os
import findertools
import getopt
import pydoc
import logging

v_argv = sys.argv[1:]

logging.basicConfig ()
log_generic = logging.getLogger ( __name__ )

try:
    v_optlist, v_args = getopt.getopt( v_argv, &quot;hv&quot; )
except Exception, e:
    print &quot;Bad command-line, caught: &quot; + str(e)
    print __doc__
    sys.exit( 1 )

b_help = False
for v_entry in v_optlist:
    if ( v_entry[0] == &quot;-v&quot; ):
        logging.getLogger ().setLevel ( 1 )
    elif ( v_entry[0] == &quot;-h&quot; ):
        b_help = True

log_generic.debug ( str(v_argv) )
log_generic.debug ( str(v_optlist) )
log_generic.debug ( str(v_args) )
log_generic.debug ( &quot;FRICKJACK&quot; )

if ( b_help or (len(v_args) != 2) ):
    #pydoc.help( __name__ )
    print __doc__
else:
    findertools.copy ( v_args[0], v_args[1] )

    </pre></code>
  </li>
  <li> pyconfirm - a short script that just pops up a dialog with a message
            and an &quot;OK&quot; button.  I find this handy when kicking
            off long tasks that I want to inform me when finished: <br />
             <center> $ sleep 20; pyconfirm &quot;Done Sleeping&quot; </center>
            <br />
         <code><pre>
$ cat ~/Library/bin/pyconfirm 
#! /usr/bin/pythonw
&quot;&quot;&quot;
Popup a dialog with the supplied message.

    pyconfirm [-h] [-v] &lt;message&gt;

REPOSITORY:
    $Id: macfaq.jsp,v 1.6 2007/08/12 00:00:44 pasquini Exp $

&quot;&quot;&quot;

import sys
import os
import getopt
import pydoc
import logging
import EasyDialogs

v_argv = sys.argv[1:]

logging.basicConfig ()
log_generic = logging.getLogger ( __name__ )

try:
    v_optlist, v_args = getopt.getopt( v_argv, &quot;hv&quot; )
except Exception, e:
    print &quot;Bad command-line, caught: &quot; + str(e)
    print __doc__
    sys.exit( 1 )

b_help = False
s_message = &quot;pyconfirm: No message&quot;

for v_entry in v_optlist:
    if ( v_entry[0] == &quot;-v&quot; ):
        logging.getLogger ().setLevel ( 1 )
    elif ( v_entry[0] == &quot;-h&quot; ):
        b_help = True

log_generic.debug ( str(v_argv) )
log_generic.debug ( str(v_optlist) )
log_generic.debug ( str(v_args) )

if ( b_help ):
    #pydoc.help( __name__ )
    print __doc__
    sys.exit(0)
elif (len(v_args) &gt; 0):
    s_message = &quot;pyconfirm: &quot;
    for s_entry in v_args:
        s_message += s_entry
        s_message += &quot; &quot;

EasyDialogs.Message( s_message )

         </pre>
</code>
     </li>
<li> finder_user_erase.py - this script is analogous to finder_copy -
         except I find myself sometimes wanting to erase a file 
         located on a mounted volume with a 
         terminal-style erase (/bin/rm) from within the Finder rather
         than have that file copied to the Trash-can.
         The following script determines which files are selected
         in the Finder, asks the Finder to popup a Confirm dialog,
         then erases the files via os.unlink.
         The script uses a macUtil method that determines the Finder selection
         by shelling out to oascript to run a snippet of AppleScript.
         There is a link to the macUtil.py module below.
         I install this script under my ~/Library/Scripts/ directory,
         so that I can easily run it from the &apos;Scripts&apos; menu which
         you can enable in your environment via the ControlPanel 
         UserPreferences tab.  So to erase a backup file off my iPod,
         I select that file, then select &apos;finder_user_erase.py&apos; from
         the Script-menu.
    <code> <pre>
#! /usr/bin/python

import sys
import os
import re

sys.path.append( os.getenv(&apos;HOME&apos;) + &quot;/Library/Python&quot; )
import littleware.base.macUtil as macUtil

v_files = macUtil.getFinderSelection()
v_clean_files = []

s_summary = &quot;&quot;&quot;
   erase the following files (/bin/rm) mounted under /Volumes ?:
       &quot;&quot;&quot;;

for s_path in v_files:
    if ( re.match( &quot;/Volumes/&quot;, s_path ) and os.path.isfile( s_path ) ):
        v_clean_files.append( s_path )
        s_summary = s_summary + s_path + &quot;,\n    &quot;

if not v_clean_files:
    s_summary = &quot;&quot;&quot;
No valid files selected.
This script will only erase FILES mounted under /Volumes/
&quot;&quot;&quot;

if &quot;OK&quot; == macUtil.popupFinderDialog ( s_summary ):
    for s_path in v_clean_files:
        try:
            os.unlink(  s_path )
        except:
            pass

         </pre></code>
     </li>
  </ul>
</p>
<br />
<pre>

$ man pythonw | cat
PYTHONW(1)                BSD General Commands Manual               PYTHONW(1)

NAME
     pythonw -- run python script with GUI

SYNOPSIS
     pythonw ...

DESCRIPTION
     pythonw is used to run python scripts that display a graphical user
     interface (GUI).  Pass the same arguments to pythonw as you would to
     python(1).  For executable scripts, use pythonw in the &quot;#!&quot; line.

SEE ALSO
     python(1)


</pre>
   </gen:description>
</gen:faq>

<gen:faq last_updated="10/01/2006"> 
    <gen:summary> Safari XML+XSLT processing </gen:summary>
    <gen:description>
      <p> <b>Note</b> (08/08/2007) - the bug described below
            has been repaired in Safari-3.
        </p>
      <p>
        Safari, Firefox, and InternetExplorer 6+ each have the cool feature that they will
        apply an XSLT style sheet to a loaded XML document if the XML doc starts with
        a reference to the style sheet like this: <br />
          <code>
&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt; <br />
&lt;?xml-stylesheet type=&quot;text/xsl&quot; href=&quot;/littleware/en/home/home.xsl&quot;?&gt;
         </code>
     </p>
     <p>   
        As of MacOS 10.4.7,
         Safari&apos;s XML/XSLT processing engine has a couple "features" that you&apos;ll
        need to workaround if you want to server out XML files that get converted
        to HTML via XSLT in the browser.  Firefox and xsltproc 
        (a great command-line XSLT processor) do not suffer from
        these problems; I have not had an opportunity to test Internet Explorer.
        The latest <a href="http://webkit.org">WebKit</a> builds also behave well,
        so hopefully the MacOS-10.5 version of Safari will include some bug fixes.
        Here are a couple problems I have run across, and the workarounds.
      <ul>
      <li> Pass-through rule infinite recursion 
            <p>
           If your XSL includes a pass-through rule like this: <br />
<code>
&lt;xsl:template match=&quot;@*|node()&quot;&gt; <br />
 &lt;xsl:copy&gt; <br />
     &lt;xsl:apply-templates select=&quot;@*|node()&quot;/&gt; <br />
 &lt;/xsl:copy&gt; <br />
&lt;/xsl:template&gt; <br />
</code>
      , then you will also need the following rule to prevent Safari from
      going into an infinite loop: <br />
<code>
&lt;!-- do not propagate xsl-stylesheet, or risk infinite loop with wildcard match --&gt; <br />
&lt;xsl:template match=&quot;processing-instruction()&quot; /&gt; <br />
</code>
          </p>
         </li>
      <li> No nested xsl:import 
             <p>
            You may only reliably use xsl:import from the root style sheet
            referenced by an XML document.
             </p>
          </li>
      </ul>
     </p>
    </gen:description>
</gen:faq>

<gen:faq last_updated="10/01/2006">
  <gen:summary>
   Airport wireless problems with Comcast cable modem 
  </gen:summary>
   <gen:description>
    <p>
     Airport has some trouble managing a network
     out to a Comcast cable modem.
     The Airport manages wireless network address translation and DHCP
     to both our powerbooks simultaneously 
     by networking the Airport through a NetGear router
        (Airport -&gt; NetGear Router -&gt; Cable Modem).
    </p>
   </gen:description>
  </gen:faq>

<gen:faq last_updated="10/01/2006"> 
  <gen:summary>
   iTunes uses AAC encoding by default
  </gen:summary>
   <gen:description>
      <p>
     Before importing music into iTunes of CD&apos;s, you may want to 
     consider changing iTunes default encoding to mp3.
     There are some advantages to AAC encoding, but not all &apos;mp3 players&apos;
     (ex - SanDisk&apos;s music player) know how to play AAC.
     I&apos;m not sure whether Windows PC&apos;s play AAC music files - probably
     they do.  It&apos;s not a big deal either way.
     </p>
   </gen:description>
    </gen:faq>
<gen:faq last_updated="10/01/2006"> 
  <gen:summary>
   iPhoto picture export  
  </gen:summary>
   <gen:description>
    <p>
     If you want to put some of your iPhoto pictures onto a web site 
     or whatever - be sure to take advantage of the &apos;File -&gt;Export&apos;
     menu - which gives you a convenient way to save out to the
     file system a scaled down copy of large resolution photos.
     I somehow missed that menu option for a while, and was
     using ImageMagic and stuff like that to try to generate
     res&apos;ed down copies of our photos.  Ugh!
    </p>
   </gen:description>
</gen:faq>

<gen:faq last_updated="10/01/2006"> 
  <gen:summary>
   Running PostgresSQL
  </gen:summary>
   <gen:description>
     <p>
     The <a href="http://www.postgres.org"> PostGres </a> database
     runs great on my powerbook.  I setup the server to run
     as the &apos;mysql&apos; user, because that user already existed
     in a disabled state in the NetInfo manager.
     The <a href="http://www.oreilly.com/catalog/macpantherian/">
           Mac OS X in a Nutshell </a> book is a great introductory reference
     to things like how to setup init.d-like startup scripts,
     how the file system is layed out (applications, system libraries),
     what the NetInfo manager (directory services) is about, etc.
     </p>
   </gen:description>
</gen:faq>

<gen:faq last_updated="10/09/2006"> 
  <gen:summary>
    Accessing Applescript from python 
  </gen:summary>
   <gen:description>
     <p>
     One of the cool things the 
        <a href="http://www.oreilly.com/catalog/macpantherian/">
        Nutshell </a>
     book mentions is the existence of /usr/bin/osascript -
     a command-line tool for running AppleScript.
     I&apos;ve only learned a little bit of AppleScript, and am
     novice at python too, but here&apos;s
     a <a href="./macUtil.py"> little python module </a> 
     that can do a few useful things.
     You can find an up-to-date version of macUtil.py
     as part of the <a href="/littleware/en/toolbox/src/home.jsf">source download</a>
     under utilities/littleware/base/macUtil.py.
     </p>
     <p> Update: since writing that macUtil module I have learned that
   there is a nice set of Mac python modules already available with
   the standard Mac python install (see the pythonw FAQ entry above).
   There are probably nicer methods to deliver AppleScript in
   one of those modules if you look around.
   </p>
   </gen:description>
</gen:faq>

<gen:faq last_updated="10/01/2006"> 
  <gen:summary>
    Some nice Open Source software to download
  </gen:summary>
   <gen:description>
    <p>
     The <a href="http://www.macgimp.org"> Gimp </a> is a nice piece
     of freeware to download, and so is 
     <a href="http://www.webweavertech.com/ovidiu/emacs.html"> emacs </a>.  
     You&apos;ll also want to install Apple&apos;s port of the
          <a href="http://www.apple.com/macosx/features/x11/"> Xfree86 server </a>,
     xcode, and all that mojo.
   </p>
  </gen:description>
</gen:faq>

<gen:faq last_updated="10/01/2006"> 
  <gen:summary>
    Starting JBOSS
  </gen:summary>
   <gen:description>
   <p> 
      I need to update this.  MacOS 10.4 no longer comes with JBoss pre-installed -
      which is not a big deal since JBOSS is a free download,
      and <i> launchd </i> provides a better way to start the server.
     </p>
   <p>
    MacOS comes with a JBoss J2EE server ready for us to start up.
    <a href="./JBoss">Here</a> is a copy of the the /Library/StartupItems/JBoss
    directory I setup to start the JBoss server at boot time.
    Note that you must add &apos;JBOSS=-YES-&apos; to /etc/hostconfig for the
    JBoss launch script to run, since the script checks for the JBOSS variable.
    Also, the default JBoss has a bug where it does not setup a complete
    CLASSPATH, so it fails to shutdown the server.
    The following patch to shutdown.sh seems to fix it up:

   <code>

 diff /tmp/shutdown.sh /Library/JBoss/3.2/bin/
52c52
&lt; JBOSS_BOOT_CLASSPATH=&quot;$JBOSS_HOME/bin/shutdown.jar:$JBOSS_HOME/client/jnet.jar&quot;
---
&gt; JBOSS_BOOT_CLASSPATH=&quot;$JBOSS_HOME/bin/shutdown.jar:$JBOSS_HOME/client/jnet.jar:$JBOSS_HOME/lib/jboss-system.jar&quot;

   </code>
  </p>
  </gen:description>

</gen:faq>

</gen:faqlist>

</gen:document>

</gen:view>

