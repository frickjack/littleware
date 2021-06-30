val littleVersion = "3.0.1"
val scala3Version = "3.0.0"

ThisBuild / organization := "littleware"
ThisBuild / scalaVersion := scala3Version
ThisBuild / version      := littleVersion

Compile / run / fork := true
Test / run / fork := true
// force json logging with log4j
// see https://logging.apache.org/log4j/2.x/log4j-jul/index.html
val jvmFlags = Seq(
  "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager",
  "-Dguice_bytecode_gen_option=DISABLED"
)
run / javaOptions ++= jvmFlags
Test / run / javaOptions ++= jvmFlags

val avro = "org.apache.avro" % "avro" % "1.10.2"
val guice = "com.google.inject" % "guice" % "5.0.1"
val guava = "com.google.guava" % "guava" % "30.1-jre"
val gson = "com.google.code.gson" % "gson" % "2.8.6"
val junit = "junit" % "junit" % "4.13.2"
// see https://stackoverflow.com/questions/28174243/run-junit-tests-with-sbt
val junitRunnerSet = Seq(junit, "com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep"))
val log4jJsonSet = Seq(
  "log4j-layout-template-json",
  "log4j-jul",
  "log4j-core"
).map({ name => "org.apache.logging.log4j" % name % "2.14.1" % Test })
val kafkaSet = Seq(
  "org.apache.kafka" % "kafka-clients" % "2.7.0",
  "org.apache.kafka" % "kafka-streams" % "2.7.0",
  "org.apache.kafka" % "kafka-streams-scala_2.13" % "2.7.0"
)
val jwtSet = Seq(
  "io.jsonwebtoken" % "jjwt-api" % "0.11.2",
  "io.jsonwebtoken" % "jjwt-impl" % "0.11.2",
  "io.jsonwebtoken" % "jjwt-gson" % "0.11.2"
)
val awsSet = Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-events" % "3.8.0",
  "com.amazonaws" % "aws-java-sdk-kms" % "1.11.996",
  "com.amazonaws" % "aws-lambda-java-runtime-interface-client" % "1.0.0"
)

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

lazy val littleScala = project
  .in(file("littleScala"))
  .dependsOn(littleware)
  .settings(
    name := "littleScala",
    libraryDependencies ++= Seq(gson, junit) ++ junitRunnerSet
  )

lazy val littleAudit = project
  .in(file("littleAudit"))
  .dependsOn(littleScala)
  .enablePlugins(PackPlugin)
  .settings(
    name := "littleAudit",
    libraryDependencies ++= Seq(avro, gson, junit % Test) ++
      awsSet ++ jwtSet ++ kafkaSet ++ junitRunnerSet
  )