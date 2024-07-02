import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

import sttp.client4.quick._
import sttp.client4.Response
import sttp.model.Uri

object ConsumerAlertProcess {
  def sendMsgWhatsApp(message: String): Unit =
  {
    val Uri: Uri = uri"https://api.callmebot.com/whatsapp.php?phone=+33695471584&text=$message&apikey=9366232"
    val response: Response[String] = quickRequest
      .get(Uri)
      .send()
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
      .option("kafka.bootstrap.servers", "localhost:9092")
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

    // TODO Appel de la fonction d'alerte
    val id = col("data.id")
    val timestamp = col("data.timestamp")
    val latitude = col("data.latitude")
    val longitude = col("data.longitude")
    val altitude = col("data.altitude")
    val dangerousity = col("data.dangerousity")

    sendMsgWhatsApp(
      s"""Alert \n
      time: ${timestamp.toString()}\n
      latitude: $latitude\n
      longitude: $longitude\n
      altitude: $altitude\n
      dangerousity: $dangerousity\n
      """)

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
