package com.leaderboard

import org.apache.kafka.clients.consumer.{ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.mockito.ArgumentMatchers.{any, argThat}
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Duration
import java.util.Collections
import java.util.concurrent.{CompletableFuture, Future}

class LeaderboardServiceTest extends AnyFlatSpec with Matchers {

  "LeaderboardService" should "consume score update events correctly" in {
    // Mock setup
    val mockConsumer: KafkaConsumer[String, String] = mock(classOf[KafkaConsumer[String, String]])
    val mockProducer: KafkaProducer[String, String] = mock(classOf[KafkaProducer[String, String]])

    // Mock behavior for Kafka Producer
    val mockFuture: Future[RecordMetadata] = CompletableFuture.completedFuture(new RecordMetadata(null, 0, 0, 0, 0L, 0, 0))
    when(mockProducer.send(any(classOf[ProducerRecord[String, String]]))).thenReturn(mockFuture)

    // Mock behavior for Kafka Consumer's poll method
    val testRecord = new ConsumerRecord[String, String]("score-updates", 0, 0L, "key", "test score update")
    val consumerRecords = new ConsumerRecords[String, String](Collections.singletonMap(new org.apache.kafka.common.TopicPartition("score-updates", 0), Collections.singletonList(testRecord)))
    when(mockConsumer.poll(any[Duration])).thenReturn(consumerRecords)

    // Create instance of LeaderboardService with mocked dependencies
    val leaderboardService = new LeaderboardService(mockConsumer, mockProducer)

    // Run the consumeScoreUpdateEvent method in a separate thread
    val consumerThread = new Thread(new Runnable {
      override def run(): Unit = {
        try {
          leaderboardService.consumeScoreUpdateEvent()
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
        }
      }
    })
    consumerThread.start()

    // Allow some time for the consumer to process the record
    Thread.sleep(2000)

    // Stop the consumer
    leaderboardService.stop()
    consumerThread.join()

    // Verify that the updateLeaderboard method was called at least once with the correct data
    verify(mockProducer, atLeastOnce()).send(argThat(new org.mockito.ArgumentMatcher[ProducerRecord[String, String]] {
      override def matches(argument: ProducerRecord[String, String]): Boolean = {
        argument.topic() == "leaderboard-updates" && argument.value() == "updated result here"
      }
    }))
  }
}
