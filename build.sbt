val littleVersion = "3.0.1"
val scala3Version = "3.0.0"

ThisBuild / organization := "littleware"
ThisBuild / scalaVersion := scala3Version
ThisBuild / version      := littleVersion
ThisBuild / scalacOptions ++= Seq("-release", "11")
ThisBuild / javacOptions ++= Seq("-source", "11", "-target", "11")

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
val gson = "com.google.code.gson" % "gson" % "2.10"
val junit4 = "junit" % "junit" % "4.13.2"
val junit5Set = Seq(
  "org.junit.jupiter" % "junit-jupiter-engine" % "5.9.1",
  "org.junit.vintage" % "junit-vintage-engine" % "5.9.1"
) 
val junitSet = junit5Set

// for running junit tests via sbt test -
// see https://stackoverflow.com/questions/28174243/run-junit-tests-with-sbt
val junit4RunnerSet = Seq("com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep"))
val junit5RunnerSet = Seq(
    "net.aichler" % "jupiter-interface" % "0.11.1" % Test
  )
val junitRunnerSet = junit5RunnerSet

val log4jJsonSet = Seq(
  "log4j-layout-template-json",
  "log4j-jul",
  "log4j-core"
  ).map({ name => "org.apache.logging.log4j" % name % "2.17.2" % Test })
val kafkaSet = Seq("kafka-clients", "kafka-streams", "kafka-streams-scala_2.13"
  ).map(name => "org.apache.kafka" % name % "2.8.2")
val jwtSet = Seq("jjwt-api", "jjwt-impl", "jjwt-gson"
  ).map(name => "io.jsonwebtoken" % name % "0.11.5")

// https://mvnrepository.com/artifact/software.amazon.awssdk/dynamodb
val awsSet = Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
  "com.amazonaws" % "aws-lambda-java-runtime-interface-client" % "2.1.1",
  "software.amazon.awssdk" % "dynamodb" % "2.16.104",
  "software.amazon.awssdk" % "kms" % "2.16.104"
)

lazy val littleware = project
  .in(file("littleware"))
  .settings(
    name := "littleware",
    crossPaths := false,
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      guice,
      guava,
      "javax.mail" % "javax.mail-api" % "1.5.6",
      "javax" % "javaee-web-api" % "8.0.1",
      "org.apache.derby" % "derby" % "10.15.2.0",
      "org.apache.derby" % "derbyclient" % "10.15.2.0",
      "org.postgresql" % "postgresql" % "42.2.27",
      "mysql" % "mysql-connector-java" % "8.0.31",
      "org.javasimon" % "javasimon-core" % "4.2.0",
      "javax.activation" % "activation" % "1.1.1"
    ) ++ junitSet ++ junitRunnerSet ++ log4jJsonSet,
  )

lazy val littleScala = project
  .in(file("littleScala"))
  .dependsOn(littleware)
  .settings(
    name := "littleScala",
    libraryDependencies ++= Seq(gson) ++ junitSet ++ junitRunnerSet
  )

lazy val littleAudit = project
  .in(file("littleAudit"))
  .dependsOn(littleScala)
  .enablePlugins(PackPlugin)
  .settings(
    name := "littleAudit",
    libraryDependencies ++= Seq(avro, gson) ++ junitSet.map(_ % Test) ++
      awsSet ++ jwtSet ++ kafkaSet ++ junitRunnerSet
  )

lazy val littleLogic = project
  .in(file("littleLogic"))
  .dependsOn(littleScala)
  .enablePlugins(PackPlugin)
  .settings(
    name := "littleLogic",
    libraryDependencies ++= Seq() ++ junit5Set.map(_ % Test) ++ junitRunnerSet
  )