

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "1.0.0-SNAPSHOT"
ThisBuild / organization     := "com.renarde"
ThisBuild / organizationName := "renarde"

lazy val root = (project in file(".")).
  settings(
    name := "producer",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.12.8",
    mainClass in Compile := Some("producer.FlowProducer")        
  )

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-stream" % "2.5.21",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "org.apache.kafka" % "kafka-clients" % "0.10.0.0",
)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"



