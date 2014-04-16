name := "BigActors"

version := "1.0"

scalaVersion := "2.11.0-RC3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "My bitbucket maven releases repo" at "https://bitbucket.org/eloipereira/maven-repo-releases/raw/master"

resolvers += DefaultMavenRepository

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.11.0-RC3"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.0-RC3"

libraryDependencies += "org.antlr" % "antlr-complete" % "3.5.2"

libraryDependencies += "bgm2java" % "bgm2java" % "1.0"

libraryDependencies += "bigraphvisualizer" % "bigraphvisualizer" % "1.0"

publishTo := Some(Resolver.file("file",  new File( "/Users/eloipereira/maven-repo/releases" )) )