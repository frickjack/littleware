# TL;DR

Setting up an [sbt](https://www.scala-sbt.org/) build to gain [scala 3](https://www.scala-lang.org/) support.

# Problem and Audience

[Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html) is an overhaul of the guts of the scala language type system and compiler that was recently released (in mid 2021) for general use.  Unfortunately, the scala plugin for [gradle](https://gradle.org/) does not yet support the new scala-3 build chain, so we ported [littleware](https://github.com/frickjack/littleware) to scala's [sbt](https://www.scala-sbt.org/) build tool.

Porting a gradle build to an sbt build is straight forward.  Both gradle and sbt define a graph of tasks for building projects, and they define the task library (types of tasks) via third-party plugins.  Both system's define the task instances in a project's build graph with a user-supplied build file written in a DSL that makes API calls against the runtime.  For example, the `littleware` sub-project in both gradle ([build.gradle](https://github.com/frickjack/littleware/blob/dev/build.gradle)) and sbt ([build.sbt](https://github.com/frickjack/littleware/blob/dev/build.sbt)) are similar to each other:

sbt:
```
lazy val littleware = project
  .in(file("littleware"))
  .settings(
    name := "littleware",
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      junit,
      guice,
      guava,
      "javax.mail" % "javax.mail-api" % "1.5.5",
      "javax" % "javaee-web-api" % "8.0",
      "org.apache.derby" % "derby" % "10.15.2.0",
      "org.apache.derby" % "derbyclient" % "10.15.2.0",
      "org.postgresql" % "postgresql" % "42.2.18",
      "mysql" % "mysql-connector-java" % "8.0.23",
      "org.javasimon" % "javasimon-core" % "4.2.0",
      "javax.activation" % "activation" % "1.1.1"
    ) ++ junitRunnerSet ++ log4jJsonSet,
  )
```

gradle:
```
project( ':littleware' ) {
    dependencies {
        implementation 'com.google.inject:guice:4.2.3:no_aop@jar'
        implementation 'junit:junit:4.13.2'
        implementation 'com.google.guava:guava:30.1-jre'
        compileOnly 'javax.mail:javax.mail-api:1.5.5'
        implementation 'javax:javaee-web-api:8.0'
        compileOnly 'org.apache.derby:derby:10.15.2.0'
        compileOnly 'org.apache.derby:derbyclient:10.15.2.0'
        compileOnly 'org.postgresql:postgresql:42.2.18'
        compileOnly 'mysql:mysql-connector-java:8.0.23'
        implementation 'org.javasimon:javasimon-core:4.2.0'
        runtimeOnly 'javax.activation:activation:1.1.1'
    }
}
```

I am glad that the port to SBT was straight forward, but I'm also annoyed that scala has its own build tool, sbt, rather than simply invest in gradle - which is widely used for building java, Android, and kotlin projects.  It would be nice if I could just learn gradle, and use it to build scala (version 3) too.  The same could be said for [golang](https://golang.org/) and [dotnet](https://dotnet.microsoft.com/) - which also implement their own build tools rather than use gradle, but gradle depends on the [jvm](https://en.wikipedia.org/wiki/Java_virtual_machine), so it makes more sense for languages in that ecosystem.  There is a trade-off between building a custom tool chain that is finely tuned for a particular domain, or using a more generic system that has a large community of users.  I expect gradle to support scala 3 in a few months anyway, so we will soon have the best of both worlds.

# Summary

It was easy to port [littleware's](https://github.com/frickjack/littleware) [gradle](https://gradle.org/) build to [sbt](https://www.scala-sbt.org/), since the two systems implement a task-graph design.
