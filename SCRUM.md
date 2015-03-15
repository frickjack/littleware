[SCRUM](http://en.wikipedia.org/wiki/Scrum_(development)) Sprint Milestones
-- elaboration phase 2

[Edit](http://code.google.com/p/littleware/w/edit/SCRUM)


# Sprints #

### 2011/02/28 - Sprint Documentation Moved ###

The littleware sprints are now documented on
[frickjack.com](http://frickjack.com/projects/littleware/).

### 2011/03/20 Milestone Sprint ###

This sprint's tasks generalize the _AuCaloging/_ web applications
for use in a generic academic library environment.  The code
currently includes several Auburn-specific assumptions.
I also intend to move this SCRUM log over to an area under
my google site at [frickjack.com](http://frickjack.com) -
which supports some nice features like edit-preview and
embedding Google calendars.
Finally, I'll update the jars in the ivy repository,
the jnlp apps, and post a littleDistro.zip file.

  * [Milestone issues](http://code.google.com/p/littleware/issues/list?can=2&q=label:Milestone-20110320&colspec=ID+Type+Status+Priority+Milestone+Component+Summary&cells=tiles)

<wiki:gadget url="http://gadgets.littleware.googlecode.com/hg/issuetracker/gcIssueTracker.xml" up\_projectName="littleware" up\_milestone="20110320" up\_sortBy="Milestone" />

### 2011/02/14 Milestone Sprint ###

This milestone focuses on tasks to prepare the _CircRequest_
webapp for use by the library's circulation department and
its patrons.

  * [Milestone issues](http://code.google.com/p/littleware/issues/list?can=2&q=label:Milestone-20110214&colspec=ID+Type+Status+Priority+Milestone+Component+Summary&cells=tiles)

<wiki:gadget url="http://gadgets.littleware.googlecode.com/hg/issuetracker/gcIssueTracker.xml" up\_projectName="littleware" up\_milestone="20110214" up\_sortBy="Milestone" />

**Summary 2011/02/18**: Although I finished a couple of the tasks for this milestone; I spent most a lot of time the last few weeks
wrapping up project at the library to handoff to Clint before
my last day (yesterday, 2011/02/17).

### 2011/01/04 Milestone Sprint ###

  * [issue 18](http://code.google.com/p/littleware/issues/detail?id=18)
  * [issue 30](http://code.google.com/p/littleware/issues/detail?id=30)
  * [issue 38](http://code.google.com/p/littleware/issues/detail?id=38)
  * [issue 37](http://code.google.com/p/littleware/issues/detail?id=37) - Done!
  * Also working to implement CircRequest webapp on task tracking infrastructure - Beta!

**Summary 2011/01/08**: I was too ambitious with this milestone, but managed
a good beta release of the circulation-request tracker that J.P. liked.
I'll focus on the circ tracker and other library-related projects for the
next milestone.

### 2010/11/21 Milestone Sprint ###

  * [issue 31](http://code.google.com/p/littleware/issues/detail?id=31)
  * [issue 30](http://code.google.com/p/littleware/issues/detail?id=30)

**Summary 2010/12/04**: simple web product browser runs, but missing many features.
We also have an updated IVY build system that includes javadoc, build-all, and build-asset publishing support. Several supporting tasks have come up including: prep standalone server release, continuous integration, and we have 2 library projects that involve task tracking.  The next milestone will focus on these supporting tasks, and we'll come back to the product tracking when that infrastructure is in place.

### 2010/10/18 Milestone Sprint ###

  * [issue 30](http://code.google.com/p/littleware/issues/detail?id=30)
  * [issue 31](http://code.google.com/p/littleware/issues/detail?id=31)

**Summary 2010/10/25**: I have a basic product-browser web layout, but only
have a start on the javascript and server-side MVC stuff.  Will continue
work over next month.

### 2010/09/12 Milestone Sprint ###

  * Develop some stand alone product manipulation tools - CLI and Swing.  First step before attempting integration with GIMP or Blender.

**Summary 2010/09/19**: simple checkin/checkout tools are passing regression tests, and started work on a web-based product browser.

### 2010/07/26 Milestone Sprint ###

  * [issue 26](http://code.google.com/p/littleware/issues/detail?id=26)

**Summary 2010/08/11**: A basic product-tracking API is in place.
I need to develop some tools that integrate with GIMP or
Blender or whatever to test if the API is useful for anything.

### 2010/06/14 Milestone Sprint ###

  * [issue 23](http://code.google.com/p/littleware/issues/detail?id=23) - Done
  * [issue 25](http://code.google.com/p/littleware/issues/detail?id=25) - Done
  * [issue 26](http://code.google.com/p/littleware/issues/detail?id=26) - Deferred

**Intro 2010/05/14**: For this milestone we'll try to develop a simple
asset-tracker for web assets like banners, stock images, and icons.
It turns out that to do this well we'll want to allow easy registration
of new asset-types and generalize the littleware module system for bootstrap.
We'll see how it goes.

**Summary 2010/06/20**: Littleware's [new module system](http://docs.google.com/Doc?docid=0Ae82pgvnWy-8ZGZ6OGc5dmhfMzRkMnNmOTRjcA&hl=en) is in place, and
most of the AuCataloging tools have been ported.  The new code also
auto-registers new asset types with the database, and extensions to the
PropertiesLoader to simplify saving and loading resources.
Unfortunately, I didn't get a start on the asset-tracker this month,
so that task is deferred till the next milestone.

### 2010/05/17 Milestone Sprint ###

  * [issue 17](http://code.google.com/p/littleware/issues/detail?id=17) - Done
  * [issue 20](http://code.google.com/p/littleware/issues/detail?id=20) - Defer
  * [issue 21](http://code.google.com/p/littleware/issues/detail?id=21) - Defer
  * [issue 23](http://code.google.com/p/littleware/issues/detail?id=23) - Defer
  * [issue 25](http://code.google.com/p/littleware/issues/detail?id=25) - Defer
  * [issue 27](http://code.google.com/p/littleware/issues/detail?id=27) - Done
  * [issue 28](http://code.google.com/p/littleware/issues/detail?id=28) - Done
  * [issue 29](http://code.google.com/p/littleware/issues/detail?id=29) - Done

**Summary 2010/05/14**: The AuCat voyager-import tool now supports OAI harvest.
The AuCat ref-stats tool integrates the new ImageManager for user thumbnails,
and is ready for beta testing.


### 2010/04/19 Milestone Sprint ###

  * [issue 17](http://code.google.com/p/littleware/issues/detail?id=17) - implement and test _littleware.apps.tracker_, and begin first use in AU reference statistics webapp
  * [issue 25](http://code.google.com/p/littleware/issues/detail?id=25) - deferred

**Summary 2010/04/20**: the littleware task-tracker API is in place with
sufficient functionality to support the AuCataloging reference statistics webapp.


### 2010/04/01 Milestone Sprint ###

  * [issue 16](http://code.google.com/p/littleware/issues/detail?id=16) - released
  * [issue 17](http://code.google.com/p/littleware/issues/detail?id=17) - in progress
  * [issue 22](http://code.google.com/p/littleware/issues/detail?id=22) - resolved
  * http://code.google.com/p/littleware/issues/detail?id=24 - resolved for 17

**Summary 2010/04/04**: The littleware task-tracking system _littleware.apps.tracker_
will extend into the next sprint.  This sprint successfully built the littleware.apps.swingbase framework, and integrated swingbase with all the _AuCataloging_ swing applications.  We introduced _littleware.apps.client.NullClientBootstrap_ to allow non-littleware applications to take advantage of littleware's bootstrap, injection, and utility functions.

# Resources #

  * [Unified Process](http://en.wikipedia.org/wiki/Unified_Process)
  * [Wiki index](http://code.google.com/p/littleware/w/list)