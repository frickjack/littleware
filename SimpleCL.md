A simple command line (CL) demo application

# Introduction #

This tutorial walks through the simpleCL demonstration application in the littleware code repository (http://code.google.com/p/littleware/source/browse/#hg/Demo/SimpleCL).  SimpleCL is a command line client that connects to a littleware server to list the assets under the _/littleware.home_ asset.

# A Simple Command #

The [SimpleCLBuilder](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/main/java/littleware/demo/simpleCL/SimpleCLBuilder.java) class holds the code that queries the littleware server and assembles the result.  The SimpleCLBuilder implements an
[AppBuilder](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/main/java/littleware/demo/simpleCL/AppBuilder.java) interface that defines a factory pattern that collects arguments (the _argv_ property) and builds a command object that executes the application command.  The _@Inject_ annotation on SimpleCLBuilder's constructor tells [Guice](http://code.google.com/p/google-guice/) to inject
SimpleCLBuilder's [AssetSearchManager](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetSearchManager.html) and [AssetPathFactory](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetPathFactory.html) dependencies.

The littleware server exports APIs to manipulate and query a graph of
[asset nodes](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/Asset.html).  Each node has a [type](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetType.html), and [HOME](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetType.html#HOME) type assets mark the roots of asset trees.  Each HOME asset has a unique name, and every asset is associated with exactly one HOME.  A typical littleware-based application design partitions its user data between different HOME assets.  The littleware engine reserves the _littleware.home_ HOME asset tree to store core assets like user and session nodes.

One way to identify an asset in the littleware graph is by its path from its HOME asset.  For example _"/littleware.home"_ is the path to the littleware.home asset, and _"/littleware.home/Users"_ is the path to the default subgraph under which littleware stores user assets.  The [AssetPathFactory](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetPathFactory.html) is a utility that parses and validates a string to build an [AssetPath](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetPath.html) object with which a client can query the littleware server.  The
[AssetSearchManager](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/asset/AssetSearchManager.html) provides a basic query API.

# Application Bootstrap #

The [CLApp](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/main/java/littleware/demo/simpleCL/CLApp.java) class holds the _main(String[.md](.md) args)_
launch function for our SimpleCL application.
The CLApp simply allocates a littleware
[ClientBootstrap](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/apps/client/ClientBootstrap.html)
object, and invokes
_bootstrap(SimpleCLBuilder.class)_
to start the littleware client environment and inject a SimpleCLBuilder object.
The ClientBootstrap constructor takes an optional "host" argument which specifies
the server-host to which the client attempts to connect.
If the host is not specified, then ClientBootstrap expects the _littleware.properties_
file to define the server URL (we'll add more information on _littleware.properties_ later).
Under the hood [Guice](http://code.google.com/p/google-guice/) manages
littleware's dependency injection,
the [Felix](http://felix.apache.org/site/index.html) OSGi implementation
manages littleware startup and shutdown,
and java [RMI](http://java.sun.com/javase/technologies/core/basic/rmi/index.jsp)
handles client-server communication.

# Regression Tests #

Littleware includes a suite of [junit](http://junit.org/) (version 3.8) tests,
and provides some infrastructure support for starting and stopping a littleware
environment within a test runner.  The SimpleCL application follows a typical
littleware testing pattern.

The first step authors test fixtures for the application classes to test.
A test fixture can take advantage of Guice's _@Inject_ annotation to
inject test dependencies.
The [SimpleCLTester](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/test/java/littleware/demo/simpleCL/SimpleCLTester.java) exercises the [SimpleCLBuilder](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/main/java/littleware/demo/simpleCL/SimpleCLBuilder.java) with a simple unit test.
Our SimpleClTester extends littleware's
[LittleTest](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/test/LittleTest.html) base class.  LittleTest extends junit's TestCase with
some methods useful for testing in littleware including a _getTestHome()_
method which return "littleware.test\_home" - the HOME asset under which
we store most test data.

Next, we assemble the test fixtures into a test suite.  Guice injects SimpleCL's
test fixtures into a
[PackageTestSuite](http://code.google.com/p/littleware/source/browse/Demo/SimpleCL/src/test/java/littleware/demo/simpleCL/PackageTestSuite.java).
A junit test-runner invokes the static _PacakgeTestSuite.suite()_ method to build
the junit TestSuite to execute, so the _suite()_ method is responsible for
bootstrapping the littleware test environment.  Littleware provides a
[TestFactory](http://wiki.littleware.googlecode.com/hg/javadoc/littleware/test/TestFactory.html)
that bootstraps the littleware environment, injects the given test suite class,
and adds a "shutdownTest" to the suite to cleanly shutdown littleware after the test runs.
The TestFactory can bootstrap either a client or server littleware environment for
client-side or server side tests.  The TestFactory can also bootstrap an embedded server
that a client environment can test against:

```
    public static TestSuite suite() {
        final ServerBootstrap bootServer = new ServerBootstrap( true );
        final ClientBootstrap bootstrap = new ClientBootstrap(
                new ClientServiceGuice( new SimpleNamePasswordCallbackHandler( "littleware.test_user", "bla"))
                );
        //return (new TestFactory()).build(bootstrap, PackageTestSuite.class );
        return (new TestFactory()).build( bootServer, bootstrap, PackageTestSuite.class );
    }
```

The embedded server still requires us to have a test database that the
server can store its data in.  We intend to enable the server
to bootstrap an embedded database in the future.