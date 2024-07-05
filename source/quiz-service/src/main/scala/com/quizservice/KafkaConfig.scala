package com.quizservice

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

object KafkaConfig {
  private val config = ConfigFactory.load().getConfig("kafka")

  // Kafka producer configuration
  val producerProps = new java.util.Properties()
  producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.servers"))
  producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("kafka.producer.key.serializer"))
  producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("kafka.producer.value.serializer"))
  val kafkaProducer = new KafkaProducer[String, QuizAnswer](producerProps)

  // Kafka consumer configuration
  val consumerProps = new java.util.Properties()
  consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.servers"))
  consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, config.getString("kafka.websocket-consumer.group.id"))
  consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getString("kafka.key.deserializer"))
  consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getString("kafka.value.deserializer"))
  consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  val kafkaConsumer = new KafkaConsumer[String, String](consumerProps)
}