<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl"?>

<jsp:root version="2.1"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:lw="http://www.littleware.com/xml/taglib/2006/general" 
 >
<![CDATA[<?xml-stylesheet type="text/xsl" href="/littleware/en/home/home.xsl" ?>]]>
  <jsp:directive.page 
          contentType="text/xml;charset=UTF-8" 
      />
  <jsp:output omit-xml-declaration="false" />
  <jsp:text>

  </jsp:text>

<jsp:scriptlet>
/** <![CDATA[

<jdoc:jspinfo
      xmlns:jdoc="http://www.littleware.com/xml/namespace/2007/jdoc"
      xmlns:xhtml="http://www.w3.org/1999/xhtml"
      xmlns="http://www.w3.org/2005/Atom"
    >
  <!-- Atom documentation entry -->
  <entry>
    <title> /en/account/logout.jsp </title>
    <author><name>Reuben</name></author>
    <link rel="alternate" href="/littleware/en/account/logout.jsp"/>
    <summary>Simple logout page - invalidates session, and says thankyou.
      </summary>
    <rights> Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com </rights>
  </entry>
</jdoc:jspinfo>

]]> */
</jsp:scriptlet>

<gen:view
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:c="http://java.sun.com/jsp/jstl/core"
      xmlns:jsp="http://java.sun.com/JSP/Page"
      xmlns="http://www.w3.org/1999/xhtml"
>
<gen:websupport>
  <c:import url="/en/lib/jspf/topmenu_default.jspf" charEncoding="UTF-8">
       <c:param name="topmenu_select" value="account" />
  </c:import>
  <c:import url="/en/lib/jspf/sidemenu_toolbox.jspf" charEncoding="UTF-8">
     <c:param name="sidemenu_select" value="vita" />
  </c:import>
</gen:websupport>
<!-- use your namespaces -->
<gen:document 
      title="Pasquini Vita"
      last_modified="02/07/2007"
      xmlns:vita="http://www.littleware.com/xml/namespace/2006/vita"
      xmlns:addr="http://www.littleware.com/xml/namespace/2006/addressbook"
      xmlns:gen="http://www.littleware.com/xml/namespace/2006/general"
      xmlns:biblio="http://www.littleware.com/xml/namespace/2006/bibliography"
      xmlns="http://www.w3.org/1999/xhtml"
     >


<vita:vita>
<addr:contact_info type="person">
   <addr:name last="Pasquini" first="Reuben" />
   <addr:email>pasquinir@bellsouth.net</addr:email>
   <addr:phone type="mobile">765-586-2458</addr:phone>

   <addr:snailmail city="Auburn" state="AL" zip="36830-2877">
   1309 Gatewood Dr. #1506
   <addr:phone>334-821-6441</addr:phone>
   </addr:snailmail>
   <gen:url>http://littleware.frickjack.com/littleware/en/toolbox/vita.html</gen:url>
</addr:contact_info>

<vita:work_experience>
  <vita:job start="09/2006" title="Software Engineer">
     <vita:employer name="littleware.frickjack.com" >
       <addr:contact_info>
           <gen:url type="external">http://www.sonypictures.com/imageworks/</gen:url>
       </addr:contact_info>
       <gen:description>
         Littleware is a small startup building a web-service framework
         for distributed data tracking and workflow support.
       </gen:description>
     </vita:employer>
     <gen:description>
          <p>
         I am the sole proprietor for 
       <a href="http://littleware.frickjack.com"> littleware.frickjack.com </a> 
       responsible for the design and implementation of a
       web-service framework for data management and workflow support.
       The tools under development leverage several technologies - 
       especially java, XML, JSP, XSLT, and RDBMS.
         </p>
     </gen:description>
  </vita:job>
  <vita:job start="09/1999" end="07/2006" title="Software Engineer">

     <vita:employer name="Sony Pictures Imageworks" >
       <addr:contact_info>
           <addr:snailmail city="Culver City" state="CA" zip="90232-2518">
           9050 West Washingtong BLVD
           <addr:phone>310-840-8000</addr:phone>
           </addr:snailmail>
           <gen:url type="external">http://www.sonypictures.com/imageworks/</gen:url>
       </addr:contact_info>
       <gen:description>
         SPI is the visual effects and animation facility responsible for the
         computer generated effects in <i>Spiderman</i>, 
         <i>Polar Express</i>,
         <i>Sea Biscuit</i>, <i>Superman Returns</i>, 
         and many other major motion
         pictures.  
       </gen:description>
     </vita:employer>
     <vita:reference name="Mitch Dobrowner" connection="Project Manager">
          <addr:email>mdobrowner@imageworks.com</addr:email>
     </vita:reference>
     <vita:reference name="Leslie Picardo" connection="boss">
          <addr:email>picardo@imageworks.com</addr:email>
     </vita:reference>
     <gen:description>
         <p>
     I was a programmer for SPI&apos;s internal software department supporting
     various in-house software projects through the entire development process.
     I acted as designer and lead software developer as a member of
     several project teams: <br /><br />
          <table border="0">
          <tr><td width="100"> Cue </td><td> render-farm job-queue management system </td></tr>
          <tr><td> DA </td><td> data-archive and tape-backup management system </td></tr>
          <tr><td> VnP </td><td> asset versioning and publishing system </td></tr>
          <tr><td> IO output </td><td> 
                  system manages the transfer of image sequences to 
                  film or HD-tape </td></tr>
          </table>
      <p>
      </p>
      I played a support and advisory role in other projects: <br /><br />
          <table border="0">
          <tr><td> Helpdesk </td><td> software and and hardware issue tracker </td></tr>
          <tr><td width="100"> VFO </td><td> shot versioning and image-tracking system </td></tr>
          <tr><td> IO in </td><td> image acquisition via film scanning and HD-tape load </td></tr>
          <tr><td> SpComponent </td><td> C++ dso version-management system </td></tr>
          </table>
        </p>
       <p>
        These projects generally required the specification, design,
       development, testing, and deployment of a request-processing system.
       We generally used of a 3-tier network-service approach
       to the design of these systems.
       The 3-tier (database, application server, client) architecture
       maintains application data in a database
       (Postgres or Oracle).  The system delivers data to clients 
       not only via jsp XML/XSLT generated web pages and reports, but
       also via  remote-procedure call APIs exposed to 
       C++, java, perl, and python 
       based clients using SOAP, RMI, or CORBA.
       </p>
       <p>
       For example, the DA-backup project delivered a system
       with multiple end-user tools and APIs.
       Different tools enabled users to submit requests for backups and restores,
       managers to manage the pending queue of requests,
       and automated backend processes to service requests
       in accordance with various rules.       
       </p>
       <p> 
         I am listed in the credits for
         <i>What Lies Beneath</i>, <i>Spiderman</i>, 
         <i>Charlie&apos;s Angels</i>, <i>Monster House</i>,
         and a few other films.
       </p>
      </gen:description>
      <vita:why_left>
        Wife got a job as a professor at Auburn University.
      </vita:why_left>
    </vita:job>

    <vita:job start="06/1998" end="09/1998" title="instructor cs250">
     <vita:employer name="Purdue University, Dept. of Computer Science" >
       <addr:contact_info>
           <addr:snailmail city="West Lafayette" state="IN" zip="47907-2107">
            305 N. University Street
           </addr:snailmail>
            <addr:phone>765-494-6010</addr:phone>
           <gen:url type="external">http://www.cs.purdue.edu </gen:url>
        </addr:contact_info>
     </vita:employer>
       <gen:description>
        <p>
        I was the instructor for cs250 - 
       &quot;Introduction to Computer Architecture&quot; -
       for the 1998 summer term.  I gave daily lectures, 
       prepared tests and quizes, assigned homework,
       and designed lab projects.
         </p>
       </gen:description>
    </vita:job>
</vita:work_experience>
<vita:education>
    <vita:degree name="Ph.D." field="Computer Science" end="08/1999" gpa="3.78/4.00">
        <vita:school name="Purdue University">
           <addr:contact_info>
             <addr:snailmail city="West Lafayette" state="IN" zip="47907-2107">
               305 N. University Street
             </addr:snailmail>
               <addr:phone number="765-494-6010" />
             <gen:url type="external">http://www.cs.purdue.edu</gen:url>
            </addr:contact_info>
        </vita:school>
        <gen:description>
         <p>
        At Purdue I carried out research under the guidance of my adviser, 
        Professor Vernon Rego, toward my dissertation: <br />
            <center><i>&quot;Algorithms for Improving the Performance of
                       Optimistic Parallel Simulation&quot;</i>. </center>
        </p>
        <p>
        The thesis explores various methods to decrease the runtime
        of an optimistic parallel discrete event simulation system
        on a network of workstations or other distributed platform.
          </p>
        </gen:description>
        <vita:publist>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Pasquini" first="Reuben" />
              <addr:name last="Rego" first="Vernon" />
              </biblio:authorlist>
              <biblio:title>Optimistic Parallel Simulation Over a Network of Workstations</biblio:title>
              <biblio:publication> 1999 Winter Simulation Conference</biblio:publication>
              <gen:date year="1999" month="12" />
              <addr:snailmail city="Phoenix" state="AZ" />
           </biblio:entry>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Pasquini" first="Reuben" />
              </biblio:authorlist>
              <biblio:title>Algorithms for Improving the Performance of Optimistic Parallel Simulation</biblio:title>
              <biblio:publication>Ph.D. thesis</biblio:publication>
              <gen:date year="1999" month="8" />
              <addr:snailmail>Purdue University</addr:snailmail>
           </biblio:entry>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Pasquini" first="Reuben" />
              <addr:name last="Rego" first="Vernon" />
              </biblio:authorlist>
              <biblio:title>Efficient Process Interaction with Threads in Parallel Discrete Event Simulation</biblio:title>
              <biblio:publication>1998 Winter Simulation Conference</biblio:publication>
              <gen:date year="1998" month="12" />
              <addr:snailmail city="Washington" state="DC" />
           </biblio:entry>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Pasquini" first="Reuben" />
              <addr:name last="Rego" first="Vernon" />
              </biblio:authorlist>
              <biblio:title>A Lazy Calendar for Optimistic Parallel Simulation</biblio:title>
              <biblio:publication>1998 Conference on Simulation Methods and Applications</biblio:publication>
              <addr:snailmail city="Orlando" state="FL" />
           </biblio:entry>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Mascarenhas" first="Edward" />
              <addr:name last="Knop" first="Felipe" />
              <addr:name last="Pasquini" first="Reuben" />
              <addr:name last="Rego" first="Vernon" />
              </biblio:authorlist>
              <biblio:title>Checkpoint and Recovery Methods in the ParaSol Simulation System</biblio:title>
              <biblio:publication>1997 Winter Simulation Conference</biblio:publication>
              <gen:date year="1997" month="12" />
              <addr:snailmail city="Atlanta" state="GA" />
           </biblio:entry>
        </vita:publist>
    </vita:degree>

    <vita:degree name="B.S." field="Computer Engineering" end="05/1994" gpa="4.7/5.0">
         <vita:school name="University of Illinois">
             <addr:contact_info>
               <addr:snailmail city="Urbana-Champaigne" state="IL" />
               <gen:url type="external">http://ece.engr.uiuc.edu</gen:url>
             </addr:contact_info>
         </vita:school>
        <vita:publist>
          <biblio:entry type="article">
              <biblio:authorlist>
              <addr:name last="Pasquini" first="Reuben" />
              <addr:name last="Loui" first="Michael" />
              </biblio:authorlist>
              <biblio:title>A Fault Tolerant Algorithm for Minimum Spanning Tree Construction on a Distributed Network</biblio:title>
              <biblio:publication>University of Illinois Technical Report #UILU-ENG-94-2210 (ACT-131)</biblio:publication>
              <gen:date year="1994" month="4" />
              <addr:snailmail city="Champaigne" state="IL" />
           </biblio:entry>
        </vita:publist>
    </vita:degree>
</vita:education>
<vita:skillset type="computer">
  <vita:skill name="java" level10="8"> 
     JDBC, junit, javadoc, logging, JMX, JAAS, RMI, CORBA, SOAP, JSP, JSTL, JSF,
     multithreading, XML SAX
  </vita:skill>
  <vita:skill name="perl" level10="8">
      DBI, POD, Tk, XML, C++ extensions, TestHarness, CGI
  </vita:skill>
  <vita:skill name="C++" level10="8">
      STL, Berkley sockets, CORBA, doxygen, POSIX threads, CppUnit
  </vita:skill>
  <vita:skill name="python" level10="6"> general programming </vita:skill>
  <vita:skill name="webservices" level10="7">
      Apache, mod_perl, Tomcat, JSP, JSTL, SOAP,
       XML, XSLT, CSS2, XHTML, some javascript
  </vita:skill>
  <vita:skill name="project management" level10="5">
     Unified Development Process, UML, 
     experience as small team lead (
     requirements gathering, design, test design, deployment,
     release management )
  </vita:skill>
  <vita:skill name="configuration management" level10="5">
     CVS, RCS, SVN, Make, dso versioning
  </vita:skill>
  <vita:skill name="linux environment" level10="8">
      csh, bash, inetd, init.d/rc, vi, emacs
  </vita:skill>
  <vita:skill name="MacOS environment" level10="8">
    Xcode, NetInfo, AppleScript
  </vita:skill>
  <vita:skill name="Windows environment" level10="6">
    Visual C++
  </vita:skill>
  <vita:skill name="database programming" level10="7">
       Oracle PL/SQL, Postgres pg/sql, Apache Derby
  </vita:skill>
</vita:skillset>
</vita:vita>

</gen:document>

</gen:view>
</jsp:root>

