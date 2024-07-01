name := "DroneDataSimulator"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "3.7.0",
  "org.apache.kafka" % "kafka-clients" % "3.7.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.1",
  "org.slf4j" % "slf4j-simple" % "1.7.30"
)

scalacOptions += "-deprecation"
