package com.quizservice

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class QuizAnswer(quizId: String, questionId: String, userId: String, answer: String)

case class QuizAnswerMessage(userId: String, questionId: String, answer: String)

object QuizAnswerMessage {
  implicit val decoder: Decoder[QuizAnswerMessage] = deriveDecoder
  implicit val encoder: Encoder[QuizAnswerMessage] = deriveEncoder
}

case class LeaderboardUpdateMessage(message: String)

object LeaderboardUpdateMessage {
  implicit val decoder: Decoder[LeaderboardUpdateMessage] = deriveDecoder
  implicit val encoder: Encoder[LeaderboardUpdateMessage] = deriveEncoder
}
