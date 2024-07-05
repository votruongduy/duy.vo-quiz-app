package com.quizservice

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import akka.util.Timeout
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WebSocketService(redisClient: RedisClientTrait,
                       kafkaConsumer: KafkaConsumer[String, String],
                       quizService: QuizService)
                      (implicit ec: ExecutionContext, materializer: Materializer, system: ActorSystem, timeout: Timeout) {

  private val clientChannel = "clients"

  //Dynamically merge messages from multiple WebSocket connections (clients).
  //Broadcast messages to all connected WebSocket clients.
  private val (sink, source) = MergeHub.source[Message]
    .toMat(BroadcastHub.sink[Message])(Keep.both)
    .run()

  def newParticipant(quizId: String): Flow[Message, Message, Any] = {
    val clientActor = system.actorOf(Props(new ClientActor))

    val messageHandlerFlow = Flow[Message]
      .collect {
        case TextMessage.Strict(txt) =>
          println(s"Received message: $txt")
          txt
      }
      .mapAsync(1) { msg =>
        println(s"Processing message: $msg")
        Future {
          decode[QuizAnswerMessage](msg) match {
            case Right(quizAnswer) =>
              quizService.publishAnswer(QuizAnswer(quizId, quizAnswer.questionId, quizAnswer.userId, quizAnswer.answer))
              TextMessage("Answer submitted"): Message
            case Left(_) =>
              decode[LeaderboardUpdateMessage](msg) match {
                case Right(leaderboardUpdate) =>
                  println(s"Parsed LeaderboardUpdateMessage: $leaderboardUpdate")
                  TextMessage(leaderboardUpdate.asJson.noSpaces): Message
                case Left(_) =>
                  println("Failed to parse message")
                  TextMessage("Invalid message format"): Message
              }
          }
        }
      }

    Flow.fromSinkAndSourceCoupledMat(
      sink.mapMaterializedValue { _ =>
        redisClient.sadd(clientChannel, clientActor.path.toString).onComplete {
          case Success(_) => println(s"Client ${clientActor.path} added.")
          case Failure(ex) => println(s"Failed to add client ${clientActor.path}: $ex")
        }
        clientActor
      },
      source
    )(Keep.right).watchTermination() { (_, termination) =>
      termination.onComplete { _ =>
        redisClient.srem(clientChannel, clientActor.path.toString).onComplete {
          case Success(_) => println(s"Client ${clientActor.path} removed.")
          case Failure(ex) => println(s"Failed to remove client ${clientActor.path}: $ex")
        }
        system.stop(clientActor)
      }
    }.via(messageHandlerFlow).watchTermination() { (_, completion) =>
      completion.onComplete { _ =>
        println("Flow completed")
      }
    }
  }

  def broadcastLeaderboard(leaderboard: String): Unit = {
    val leaderboardMessage = LeaderboardUpdateMessage(leaderboard).asJson.noSpaces
    redisClient.smembers(clientChannel).onComplete {
      case Success(clients) =>
        clients.foreach { clientPath =>
          system.actorSelection(clientPath).resolveOne().onComplete {
            case Success(actorRef) => actorRef ! TextMessage(leaderboardMessage)
            case Failure(ex) => println(s"Failed to resolve client $clientPath: $ex")
          }
        }
      case Failure(ex) => println(s"Failed to retrieve clients from Redis: $ex")
    }
    Source.single(TextMessage(leaderboardMessage): Message).runWith(sink)
  }

  def startConsumingLeaderboardUpdates(): Unit = {
    Future {
      while (true) {
        val records = kafkaConsumer.poll(java.time.Duration.ofMillis(100)).asScala
        for (record <- records) {
          val leaderboardUpdate = record.value()
          println(s"Received leaderboard update: $leaderboardUpdate")
          broadcastLeaderboard(leaderboardUpdate)
        }
      }
    }
  }

  class ClientActor extends akka.actor.Actor {
    override def receive: Receive = {
      case msg: Message => context.parent ! msg
    }
  }
}