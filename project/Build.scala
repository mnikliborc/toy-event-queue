import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "simple-event-queue"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Select Play modules
    //jdbc,      // The JDBC connection pool and the play.api.db APIt
    //anorm,     // Scala RDBMS Library
    //javaJdbc,  // Java database API
    //javaEbean, // Java Ebean plugin
    //javaJpa,   // Java JPA plugin
    //filters,   // A set of built-in filters
    javaCore,  // The core Java API
  
    // WebJars pull in client-side web libraries
    "org.webjars" % "webjars-play" % "2.1.0",
    "org.webjars" % "bootstrap" % "2.3.1",
  
    // Akka
    "com.typesafe.akka" %% "akka-actor" % "2.3.4",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.4",
    "com.typesafe.akka" %% "akka-slf4j"    % "2.3.4"
  )
  
  resolvers += "akka" at "http://repo.akka.io/snapshots"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.1"
    // Add your own project settings here      
  )

}
