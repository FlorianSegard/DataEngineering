name := "DroneDataAnalysis"

version := "0.1"

scalaVersion := "2.12.10"

val sparkVersion = "3.5.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

  "org.apache.hadoop" % "hadoop-aws" % "3.2.0",

  "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.375"
)