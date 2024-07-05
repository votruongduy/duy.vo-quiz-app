package com.quizservice

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class QuizService(producer: KafkaProducer[String, QuizAnswer]) {

  def publishAnswer(quizAnswer: QuizAnswer): Unit = {

    val record = new ProducerRecord[String, QuizAnswer]("quiz-answers", quizAnswer.quizId, quizAnswer)
    producer.send(record)

    updateDB(quizAnswer)
  }

  private def updateDB(quizAnswer: QuizAnswer) = {
    // TODO: Update database here
  }
}
