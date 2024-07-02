import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import sttp.client4.quick._
import sttp.client4.Response
import sttp.model.Uri

import scala.annotation.tailrec

object ConsumerAlertProcess {
  def sendMsgWhatsApp(message: String): Unit = {
    val Uri: Uri = uri"https://api.callmebot.com/whatsapp.php?phone=+33695471584&text=$message&apikey=9366232"
    val response: Response[String] = quickRequest
      .get(Uri)
      .send()
  }
  @tailrec
  def sendsAlertRec(rows: List[Row]) : Unit = {
    sendMsgWhatsApp("test")
    rows match {
      case Nil => None
      case head :: rows => {
        val timestamp = head.getAs[Long]("timestamp")
        val latitude = head.getAs[Double]("latitude")
        val longitude = head.getAs[Double]("longitude")
        val altitude = head.getAs[Double]("altitude")
        val dangerousity = head.getAs[Double]("dangerousity")
        val message =
          s"""Alert \n
          time: $timestamp\n
          latitude: $latitude\n
          longitude: $longitude\n
          altitude: $altitude\n
          dangerousity: $dangerousity\n"""
        sendMsgWhatsApp(message)
        sendsAlertRec(rows)      }
    }
  }

    def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("Drone Data Processor")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Définition du schéma JSON selon la structure de DroneData
    val droneDataSchema = new StructType()
      .add("id", IntegerType)
      .add("timestamp", LongType)
      .add("latitude", DoubleType)
      .add("longitude", DoubleType)
      .add("altitude", DoubleType)
      .add("dangerousity", DoubleType)

    // Lecture des données depuis Kafka
    val rawStream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092, localhost:9093")
      .option("subscribe", "high-danger-alerts")
      .option("group.id", "groupalert")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING) as message")

    // Désérialisation des messages JSON en DataFrame en utilisant le schéma défini
    val droneDataStream = rawStream
      .select(from_json($"message", droneDataSchema).as("data"))
      .select("data.*")

    val query = droneDataStream.writeStream
      .outputMode("append")
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        sendsAlertRec(batchDF.collect().toList)
      }
      .start()

    query.awaitTermination()

    val processQuery = droneDataStream
      .selectExpr("CAST(id AS STRING) AS key", "to_json(struct(*)) AS value")
      .writeStream
      .format("console")
      .outputMode("update")
      .trigger(Trigger.ProcessingTime("1 minute"))
      .option("truncate", false)
      .start()
    processQuery.awaitTermination()
  }
}
