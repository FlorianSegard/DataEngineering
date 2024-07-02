import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._



object ConsumerAlert {
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
      //.option("kafka.bootstrap.servers", "localhost:9092")
      .option("kafka.bootstrap.servers", "localhost:9092,localhost:9093")
      .option("subscribe", "drone-data")
      .option("group.id", "groupalert")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING) as message")

    // Désérialisation des messages JSON en DataFrame en utilisant le schéma défini
    val droneDataStream = rawStream
      .select(from_json($"message", droneDataSchema).as("data"))
      .select("data.*")

    // Filtrage des données pour dangerousity > 0.9
    val highDangerStream = droneDataStream
      .filter($"dangerousity" > 0.9)

    // Écriture des données filtrées (high dangerousity) vers un autre système (ex: Kafka)
    val highDangerQuery = highDangerStream
      .selectExpr("CAST(id AS STRING) AS key", "to_json(struct(*)) AS value")
      .writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092, localhost:9093")
      .option("topic", "high-danger-alerts")
      .option("checkpointLocation", "checkpoint/high-danger")
      .outputMode("update")
      .trigger(Trigger.ProcessingTime("1 minute"))
      .start()


    highDangerQuery.awaitTermination()
  }
}
