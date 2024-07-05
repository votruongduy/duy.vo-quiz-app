package com.leaderboard

object Main extends App {

  val consumer = KafkaConfig.consumer
  consumer.subscribe(java.util.Arrays.asList("score-updates"))

  val producer = KafkaConfig.producer

  val leaderboardService = new LeaderboardService(consumer, producer)
  leaderboardService.consumeScoreUpdateEvent()
}