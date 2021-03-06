
// name of project
name := "art-generator-s"

// version of project
version := "0.6"

scalaVersion := "2.12.4"

// sometimes this program needs a bit more memory than the default
// this allows that behavior
fork := true
javaOptions += "-Xms512M"
javaOptions += "-Xmx6G"
javaOptions += "-Djava.rmi.server.hostname=localhost"

// define main class explicitly
mainClass in Compile := Some("me.paul.artgenerators.ArtGeneratorSwingApp")

assemblyJarName in assembly := name.value + "_" + scalaVersion.value + "-" + version.value + "_fat-executable.jar"

// dependency on scala-swing
libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"