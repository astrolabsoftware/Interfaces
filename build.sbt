name := "tjna"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "net.java.dev.jna" % "jna" % "4.5.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.1.0"  % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided"

