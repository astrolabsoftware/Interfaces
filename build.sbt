/*
 * Copyright 2018 Christian Arnault
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Dependencies._
import xerial.sbt.Sonatype._

lazy val root = (project in file(".")).
 settings(
   inThisBuild(List(
     version      := "0.1.0"
   )),
   scalaVersion := "2.11.8",
   // Name of the application
   name := "Interfaces",
   // Name of the orga
   organization := "com.github.theastrolab",
   // Do not execute test in parallel
   parallelExecution in Test := false,
   // Fail the test suite if statement coverage is < 70%
   coverageFailOnMinimum := true,
   coverageMinimum := 70,
   // Put nice colors on the coverage report
   coverageHighlighting := true,
   // Do not publish artifact in test
   publishArtifact in Test := false,
   // Exclude runner class for the coverage
   coverageExcludedPackages := "<empty>;com.spark3d.examples*",

   libraryDependencies ++= Seq(
     "net.java.dev.jna" % "jna" % "4.5.1" % "provided",
     "org.apache.spark" %% "spark-core" % "2.1.0" % "provided",
     "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided",
     "org.scalactic" %% "scalactic" % "3.0.5" % "test",
     "org.scalatest" %% "scalatest" % "3.0.5" % "test"
   )
 )


//      scalaTest % Test

// POM settings for Sonatype

homepage := Some(
 url("https://github.com/astrolabsoftware/Interfaces")
)

scmInfo := Some(
 ScmInfo(
   url("https://github.com/astrolabsoftware/Interfaces"),
   " https://github.com/astrolabsoftware/Interfaces.git"
 )
)

developers := List(
 Developer(
   "JulienPeloton",
   "Julien Peloton",
   "peloton@lal.in2p3.fr",
   url("https://github.com/JulienPeloton")
 ),
 Developer(
 "ChristianArnault",
 "Christian Arnault",
 "arnault@lal.in2p3.fr",
 url("https://github.com/ChristianArnault")
 ),
 Developer(
 "mayurdb",
 "Mayur Bhosale",
 "mayurdb31@gmail.com",
 url("https://github.com/mayurdb")
 )
)

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

publishMavenStyle := true

publishTo := {
 val nexus = "https://oss.sonatype.org/"
 if (isSnapshot.value)
  Some("snapshots" at nexus + "content/repositories/snapshots")
 else
  Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

