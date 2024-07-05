name := "QuizService"

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
  "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
  "com.esotericsoftware" % "kryo" % "5.6.0",
  "com.github.etaty" %% "rediscala" % "1.9.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
  "io.circe" %% "circe-generic-extras" % "0.14.1",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.5.3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.8.6" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.8.6" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.mockito" %% "mockito-scala" % "1.17.37" % Test,
  "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test
)

scalacOptions += "-Ymacro-annotations"