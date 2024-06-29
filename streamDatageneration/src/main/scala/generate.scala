import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import java.util.Properties
import scala.util.Random
import scala.annotation.tailrec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class DroneData(id: Int, timestamp: Long, latitude: Double, longitude: Double, altitude: Double, dangerousity: Double)

object DataGenerator {

  def createProducer: KafkaProducer[String, String] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer[String, String](props)
  }

  def objectMapper: ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)  // Register the DefaultScalaModule
    mapper
  }

  def nextDoubleInRange(min: Double, max: Double): Double = {
    val rand = new Random()
    rand.nextDouble() * (max - min) + min
  }

  def generateData(id: Int): DroneData = {
    val rand = new Random()
    DroneData(
      id = id,
      timestamp = System.currentTimeMillis(),
      latitude = nextDoubleInRange(-90.0, 90.0),
      longitude = nextDoubleInRange(-180.0, 180.0),
      altitude = nextDoubleInRange(0.0, 12000.0),
      dangerousity = rand.nextDouble()
    )
  }

  @tailrec
  def sendDataRecursively(producer: KafkaProducer[String, String], topic: String, currentId: Int): Unit = {
    val data = generateData(currentId)
    val jsonData = objectMapper.writeValueAsString(data)
    val record = new ProducerRecord[String, String](topic, currentId.toString, jsonData)
    producer.send(record)
    Thread.sleep(1000)
    sendDataRecursively(producer, topic, currentId + 1)
  }

  def main(args: Array[String]): Unit = {
    val producer = createProducer
    val topic = "drone-data"

    sendDataRecursively(producer, topic, 1)
    producer.close()
  }
}

