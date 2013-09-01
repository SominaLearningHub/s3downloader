import com.typesafe.sbt.SbtStartScript

name := "s3downloader"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.5.5"

libraryDependencies += "org.specs2" % "specs2_2.10" % "2.1.1"

seq(SbtStartScript.startScriptForClassesSettings: _*)