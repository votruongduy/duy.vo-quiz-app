name := "LeaderBoard"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-stream" % "2.8.6",
  "org.mongodb.scala" %% "mongo-scala-driver" % "5.1.1",
  "ch.qos.logback" % "logback-classic" % "1.5.6",
  "org.apache.kafka" %% "kafka" % "3.7.1",
  "org.apache.kafka" % "kafka-clients" % "3.7.1",
  "org.apache.kafka" % "kafka-streams" % "3.7.1",
  "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.0",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.mockito" %% "mockito-scala" % "1.16.42" % Test,
  "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test
)
