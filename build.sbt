name := "BigActors"

version := "1.0"

scalaVersion := "2.10.0"

crossPaths := false

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "My bitbucket maven releases repo" at "https://bitbucket.org/eloipereira/maven-repo-releases/raw/master"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "AFA Maven repo" at "https://bitbucket.org/pmosilva/maven2-release/raw/master/seagull-rosjava-libs"

resolvers += DefaultMavenRepository

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.0"

libraryDependencies += "org.antlr" % "antlr-complete" % "3.5.2"

libraryDependencies += "bgm2java" % "bgm2java" % "1.1"

libraryDependencies += "bigraphvisualizer" % "bigraphvisualizer" % "1.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4-SNAPSHOT"

libraryDependencies += "pt.edu.academiafa" % "seagull-rosjava-lib" % "0.0.2"

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("file",  new File( Path.userHome.absolutePath+"/.m2/repository/snapshots" )) )
  else
    Some(Resolver.file("file",  new File( Path.userHome.absolutePath+"/.m2/repository/releases" )) )
}
