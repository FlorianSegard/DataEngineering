name := "DroneDataProcessor"

version := "1.0"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.1.1",
  "org.apache.spark" %% "spark-sql" % "3.1.1",
  "org.apache.spark" %% "spark-streaming" % "3.1.1",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.1.1",
  "org.apache.kafka" % "kafka-clients" % "2.8.0"
)

