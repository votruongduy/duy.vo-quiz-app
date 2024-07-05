package com.quizservice

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import akka.util.Timeout
import io.circe.syntax._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class WebSocketServiceTest extends TestKit(ActorSystem("WebSocketServiceTest"))
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with MockitoSugar {

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer = Materializer(system)
  implicit val timeout: Timeout = Timeout(5.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "WebSocketService" should {
    "handle new participants, process messages, and broadcast leaderboard updates" in {
      val redisClientMock = new MockRedisClient
      val kafkaConsumerMock = mock[KafkaConsumer[String, String]]
      val quizServiceMock = mock[QuizService]

      // Correctly mock publishAnswer to return Unit
      doAnswer(_ => ()).when(quizServiceMock).publishAnswer(any[QuizAnswer])

      val webSocketService = new WebSocketService(redisClientMock, kafkaConsumerMock, quizServiceMock)

      // Create a Flow from the newParticipant method
      val flowUnderTest = webSocketService.newParticipant("quizId")

      // Create a TestSource and TestSink to simulate WebSocket messages
      val (wsSource, wsSink) = TestSource.probe[Message]
        .via(flowUnderTest)
        .toMat(TestSink.probe[Message])(Keep.both)
        .run()

      println("Sending message to WebSocket flow...")
      // Send a message to the WebSocket flow
      val testMessage = QuizAnswerMessage("user1", "question1", "answer1").asJson.noSpaces
      wsSource.sendNext(TextMessage(testMessage))

      // Verify the flow acknowledges receipt of the answer
      println("Expecting acknowledgment message...")
      wsSink.request(1)
      wsSink.expectNext(10.seconds, TextMessage.Strict("Answer submitted"))
      println("Received acknowledgment message")

      // Verify QuizService published the answer
      verify(quizServiceMock).publishAnswer(QuizAnswer("quizId", "question1", "user1", "answer1"))

      // Simulate broadcasting leaderboard update
      val leaderboardUpdateMessage = LeaderboardUpdateMessage("Leaderboard update").asJson.noSpaces
      webSocketService.broadcastLeaderboard("Leaderboard update")

      // Verify the broadcast message
      wsSink.request(1)
      wsSink.expectNext(10.seconds, TextMessage.Strict(leaderboardUpdateMessage))
      println("Received leaderboard update")

      // Ensure no other messages are sent
      wsSource.sendComplete()
      wsSink.expectComplete()
    }
  }
}
