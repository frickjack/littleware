## Welcome to Littleware! ##

## What is littleware? ##

Littleware is a collection of java libraries that implement several utilities, applications, and services.  First, littleware provides application infrastructure like a simple module system with dependency injection (via guice), bootstrap and shutdown (with a little OSGi), JAAS authentication, and other support for GUI, JNDI, CLI, and web applications.

Another aspect of littleware is a node-store service that exports client APIs (currently via RMI) that manipulate a graph (usually a tree) of nodes representing different types of assets (users, files, folders, tasks) depending on the end user application.

Finally, the littleware repository includes various applications that leverage the littleware APIs.  The apps range from a JNDI tool that converts MARC (library record) files between different formats to a simple Swing browser of a littleware node-store to a library request-tracker that tracks patron requests (like pull a book from storage) as nodes in a node-store.

There's more introduction in this [bLog entry](http://blog.frickjack.com/2011/03/little-plans.html).

Ugh - unfortunatley - a lot of this is out of date.
The broad strokes are the same, but I just pusehd the "littleware 3.0"
dev code to the main hg repo.  That code has a bunch of patches that extend
littleware to support webapps like [little ToDo](http://apps.frickjack.com/littleware_apps/eventTrack/littleToDo.html), and I'm working to get everything ready for a littleware 3.0 release.  Stay tuned!

  * API documentation
    * [javadoc](http://wiki.littleware.googlecode.com/hg/javadoc/index.html)
    * [scaladoc](http://wiki.littleware.googlecode.com/hg/scaladoc/index.html)
    * [ydoc (javascript)](http://wiki.littleware.googlecode.com/hg/ydoc/index.html)
  * [Getting Started](ServerSetup.md) instructions
  * [blog.frickjack.com](http://blog.frickjack.com)
  * Checkout the [SCRUM](http://www.frickjack.com/projects/littleware/) for the current dev schedule
  * [Latest news](http://frickjack.com) at frickjack.com
  * ReleaseNotes
  * [Jenkins](http://beta.frickjack.com:8080/jenkins/) continuous integration server (available when I'm online)


## Sub Projects ##
### Library Csharp ###

The [libsharp](http://code.google.com/p/littleware/source/checkout?repo=libsharp) repository tracks some C# code that builds on Voyager's batchcat dll and the Oracle ADO assemblies to build tools that both query and update MARC records in Auburn's Voyager ILS.

  * [sandcastle C# docs](http://wiki.littleware.googlecode.com/hg/sandcastle/index.htm)

Dependencies:

  * batchcat  Voyager comes with this dll that provides functions for updating the catalog in various ways. We reference as a COM object.
  * Oracle, .NET, Microsoft Oracle Data Provider for ADO .NET  Install the Oracle client, and set the TNS\_NAMES environment. We currently use the Microsoft Oracle Data Provider - I think that's included with .NET.
  * log4net
  * NUnit

### AU Cataloging ###

The [catalog](http://code.google.com/p/littleware/source/checkout?repo=catalog) repository tracks APIs and apps (swing, webstart, and cli) mostly developed in scala for manipulating library MARC and DSpace records, harvesting data from different feeds, and generally just moving data from A to B.  The code depends has not been maintained in a while, and depends on an old the littleware-2.4 jars, so it needs some love to build with the latest code.