package com.quizservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import redis.RedisClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App {

  val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("quiz-service-system")
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  private val kafkaProducer = KafkaConfig.kafkaProducer
  val kafkaConsumer = KafkaConfig.kafkaConsumer
  kafkaConsumer.subscribe(java.util.Collections.singletonList(config.getString("kafka.websocket-consumer.topic")))

  // Initialize Redis client
  val redis = RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
  val redisClient = new RedisClientImpl(redis)

  // Initialize QuizService
  val quizService = new QuizService(kafkaProducer)

  // Initialize WebSocketService with correct parameter order
  val webSocketService = new WebSocketService(redisClient, kafkaConsumer, quizService)

  // Start consuming leaderboard updates
  webSocketService.startConsumingLeaderboardUpdates()

  // Define WebSocket route
  private val route: Route =
    path("quiz" / Segment) { quizId =>
      handleWebSocketMessages(webSocketService.newParticipant(quizId))
    }

  private val httpHost = config.getString("http.host")
  private val httpPort = config.getInt("http.port")

  // Start HTTP server
  private val bindingFuture = Http().newServerAt(httpHost, httpPort).bind(route)

  println(s"Server online at http://$httpHost:$httpPort/...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => {
      system.terminate()
    })
}
