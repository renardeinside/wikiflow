

// ThisBuild / scalaVersion     := "2.12.8"
// ThisBuild / version          := "1.0.0-SNAPSHOT"
// ThisBuild / organization     := "com.renarde"
// ThisBuild / organizationName := "renarde"

lazy val root = (project in file(".")).
  settings(
    name := "consumer",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.12",
    mainClass in Compile := Some("consumer.FlowConsumer")        
  )

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("reference.conf") => MergeStrategy.concat
    case x => MergeStrategy.first
}

libraryDependencies += "io.druid" %% "tranquility-core" % "0.8.3"
libraryDependencies += "io.druid" %% "tranquility-spark" % "0.8.3"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.0" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.4.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"





