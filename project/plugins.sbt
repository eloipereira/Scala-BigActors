resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("info.schleichardt" % "sbt-sonar" % "0.2.0-SNAPSHOT")

addSbtPlugin("com.sqality.scct" % "sbt-scct" % "0.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

val reporterPlugin = ProjectRef(new URI("git://github.com/mmarich/sbt-simple-junit-xml-reporter-plugin.git"),
    "sbt-simple-junit-xml-reporter-plugin")
lazy val plugins = Project("plugins", file(".")).dependsOn(reporterPlugin)

