package com.leaderboard

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

import java.util.Properties

object KafkaConfig {
  private val config = ConfigFactory.load().getConfig("kafka")

  private val consumerProps = {
    val props = new Properties()
    props.put("bootstrap.servers", config.getString("bootstrap.servers"))
    props.put("group.id", config.getString("group.id"))
    props.put("key.deserializer", config.getString("key.deserializer"))
    props.put("value.deserializer", config.getString("value.deserializer"))
    props
  }

  private val producerProps = {
    val props = new Properties()
    props.put("bootstrap.servers", config.getString("bootstrap.servers"))
    props.put("key.serializer", config.getString("key.serializer"))
    props.put("value.serializer", config.getString("value.serializer"))
    props
  }

  val consumer = new KafkaConsumer[String, String](consumerProps)
  val producer = new KafkaProducer[String, String](producerProps)
}