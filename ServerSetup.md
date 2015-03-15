Getting Started with Littleware

# Quick Start #

  1. Download the latest littleDistro.zip file
  1. Unzip and install to some location you like - I use ~/littleware
  1. Add ~/littleware/client/bin to your PATH
  1. Change the littleware.administrator password in ~/littleware/server/config/passwords.txt - _littleware.administrator_ is the littlware 'root' user.  You may also add other user=password pairs to passwords.txt, or modify login.config to use LDAP or whatever.
  1. Start the server: _~/littleware/server/bin/littleServer.bat_ (or littleServer.sh on linux/Mac ...)
  1. Browse the server: _~/littleware/client/bin/lgo.bat browse_


# Building from source code #

  1. Install [Mercurial](http://mercurial.selenic.com/) (hg) source control management software
  1. Install the [jdk](http://www.oracle.com/technetwork/java/javase/downloads/index.html) for java6 or newer
  1. Install [scala](http://scala-lang.org) 2.8.1 or newer
  1. Install [apache ant](http://ant.apache.org/) 1.8 or newer
  1. Clone the _littleware_ source
```
hg clone https://littleware.googlecode.com/hg/ littleware
```
  1. Clone the _catalog_ source to a _Catalog/_ folder under the _littleware_ code
```
cd littleware;
hg clone https://catalog.littleware.googlecode.com/hg/ Catalog
```
  1. The _littleware_ and _catalog_ Mercurial repositories each contain several java or scala sub-projects that each build some code asset (.jar), and manage interdependencies with the [apache ivy](http://ant.apache.org/ivy/) ant extension.  The root build script (`littleware/build.xml`) has a rules to build (_buildAll_) and test (_buildAndTest_) most of the subprojects.  Most of the sub-projects are setup to work with the [NetBeans IDE](http://netbeans.org).
```
cd littleware
ant buildAndTest
```


# Glassfish Setup #

A littleware server may run as a webapp in a [Glassfish](http://glassfish.dev.java.net) or other container - just register the littleware.security.auth.server.BootstrapServlet in web.xml.

...
I'm familiar with glassfish, so here are some tips.
Modify the server-launch java command-line options either by editing the config/domain.xml directly or via the asadmin configuration tool.  Different server environments may not require these changes.

  * -server runs the JVM in server mode (remove -client)
  * -Xms2048m -Xmx2048m gives the server 2GB of heap - or as much memory
> > as you require
  * -Djava.rmi.server.hostname=hostname.com specifies the host for RMI
> > services to advertise themselves under
  * -Djava.awt.headless=true to shortcut code that attempts to access the display

Enable user authentication by the littleware server by
registering a 'littleware.login' JAAS authentication module with the
glassfish runtime.  The domain/config/login.conf file specifies the
JAAS authentication modules that glassfish makes available to webapps.