package com.quizservice

import redis.RedisClient

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait RedisClientTrait {
  def sadd(key: String, value: String): Future[Boolean]
  def srem(key: String, value: String): Future[Boolean]
  def smembers(key: String): Future[Set[String]]
}


class RedisClientImpl(redis: RedisClient) extends RedisClientTrait {
  override def sadd(key: String, value: String): Future[Boolean] =
    redis.sadd(key, value).map(_ > 0)

  override def srem(key: String, value: String): Future[Boolean] =
    redis.srem(key, value).map(_ > 0)

  override def smembers(key: String): Future[Set[String]] =
    redis.smembers[String](key).map(_.toSet)
}
