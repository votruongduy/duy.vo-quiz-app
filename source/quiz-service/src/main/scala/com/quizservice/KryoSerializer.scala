package com.quizservice

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.apache.kafka.common.serialization.{Deserializer, Serializer}

class KryoSerializer extends Serializer[QuizAnswer] {
  private val kryo = new Kryo()
  kryo.register(classOf[QuizAnswer])

  override def serialize(topic: String, data: QuizAnswer): Array[Byte] = {
    val output = new Output(1024, -1)
    kryo.writeObject(output, data)
    output.close()
    output.toBytes
  }

  override def close(): Unit = {}

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {}
}

class KryoDeserializer extends Deserializer[QuizAnswer] {
  private val kryo = new Kryo()
  kryo.register(classOf[QuizAnswer])

  override def deserialize(topic: String, data: Array[Byte]): QuizAnswer = {
    val input = new Input(data)
    val obj = kryo.readObject(input, classOf[QuizAnswer])
    input.close()
    obj
  }

  override def close(): Unit = {}

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {}
}
