package com.quizservice

import scala.concurrent.Future

class MockRedisClient extends RedisClientTrait {
  private var data = Set[String]()

  override def sadd(key: String, value: String): Future[Boolean] = {
    val added = !data.contains(value)
    data += value
    Future.successful(added)
  }

  override def srem(key: String, value: String): Future[Boolean] = {
    val removed = data.contains(value)
    data -= value
    Future.successful(removed)
  }

  override def smembers(key: String): Future[Set[String]] = {
    Future.successful(data)
  }
}
