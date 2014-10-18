
name := "BigActors"

//version := "1.0"

releaseSettings

scalaVersion := "2.10.0"

crossPaths := false

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "My bitbucket maven releases repo" at "https://bitbucket.org/eloipereira/maven-repo-releases/raw/master"

resolvers += "My bitbucket maven snapshots repo" at "https://bitbucket.org/eloipereira/maven-repo-snapshots/raw/master"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "AFA Maven repo" at "https://bitbucket.org/ciafa-sw/maven2-release/raw/master/seagull-rosjava-libs"

resolvers += DefaultMavenRepository

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.0"

libraryDependencies += "org.antlr" % "antlr-complete" % "3.5.2"

libraryDependencies += "bgm2java" % "bgm2java" % "0.1.2"

libraryDependencies += "bigraphvisualizer" % "bigraphvisualizer" % "1.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4-SNAPSHOT"

libraryDependencies += "pt.edu.academiafa" % "seagull-rosjava-lib" % "0.0.2"

libraryDependencies += "commons-logging" % "commons-logging" % "1.2"

libraryDependencies += "log4j" % "log4j" % "1.2.17"

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("file",  new File( Path.userHome.absolutePath+"/.m2/repository/snapshots" )) )
  else
    Some(Resolver.file("file",  new File( Path.userHome.absolutePath+"/.m2/repository/releases" )) )
}
