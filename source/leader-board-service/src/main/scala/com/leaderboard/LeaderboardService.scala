package com.leaderboard

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.LoggerFactory

import java.time.Duration
import scala.collection.JavaConverters._

class LeaderboardService(val consumer: KafkaConsumer[String, String], val producer: KafkaProducer[String, String]) {

  private val log = LoggerFactory.getLogger(classOf[LeaderboardService])

  @volatile private var isRunning = true

  def stop(): Unit = {
    isRunning = false
  }

  def consumeScoreUpdateEvent(): Unit = {
    while (isRunning) {
      try {
        val records = consumer.poll(Duration.ofMillis(100)).asScala

        if (records.isEmpty) {
          log.debug("No records received in this poll interval.")
        } else {
          records.foreach { record =>
            val data = record.value()
            log.info(s"Processing record with data: $data")
            // process the score update event
            val updatedBoard = updateLeaderboard(data)

            // send the final info to kafka
            publishUpdatedLeaderboard(updatedBoard)
            updateDb(updatedBoard)
          }
        }
      } catch {
        case e: Exception =>
          log.error("Error while consuming score update event: ", e)
      }
    }
    log.info("LeaderboardService stopped.")
  }

  private def updateLeaderboard(data: String): String = {
    log.info(s"Updating leaderboard with data: $data")
    // Implement the logic to update the leaderboard
    println("logic to update leaderboard here")
    "updated result here"
  }

  private def parseQuizId(data: String): String = {
    log.info(s"Parsing quiz ID from data: $data")
    // Implement logic to parse quiz ID from data
    "quizId"
  }

  private def publishUpdatedLeaderboard(data: String): Unit = {
    try {
      // parse quiz id from the data
      val quizId = parseQuizId(data)
      log.info(s"Publishing updated leaderboard with quiz ID: $quizId and data: $data")
      val leaderboardRecord = new ProducerRecord[String, String]("leaderboard-updates", quizId, data)
      producer.send(leaderboardRecord)
    } catch {
      case e: Exception =>
        log.error("Error while publishing updated leaderboard: ", e)
    }
  }

  private def updateDb(data: String): Unit = {
    log.info(s"Updating database with data: $data")
    // Implement logic to update the database
    println("update db logic here")
  }
}
