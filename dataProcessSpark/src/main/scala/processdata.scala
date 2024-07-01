import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object DroneDataProcessor {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("Drone Data Processor")
      .master("local[*]")  // Utilisez "local[*]" pour exécuter avec tous les cœurs locaux ou configurez pour votre environnement de cluster.
      .getOrCreate()

    import spark.implicits._

    println("Spark Session created successfully.")

    // Définition du schéma JSON selon la structure de DroneData
    val droneDataSchema = new StructType()
      .add("id", IntegerType)
      .add("timestamp", LongType)
      .add("latitude", DoubleType)
      .add("longitude", DoubleType)
      .add("altitude", DoubleType)
      .add("dangerousity", DoubleType)

    println("Schema defined successfully.")

    try {
      // Lecture des données depuis Kafka
      val rawStream = spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", "kafka:9092")
        .option("subscribe", "drone-data")
        .load()
        .selectExpr("CAST(value AS STRING) as message")

      println("Kafka stream read successfully.")

      // Désérialisation des messages JSON en DataFrame en utilisant le schéma défini
      val droneDataStream = rawStream
        .select(from_json($"message", droneDataSchema).as("data"))
        .select("data.*")

      println("Data deserialization completed successfully.")

      // Écriture des données structurées dans des fichiers JSON
      val query = droneDataStream.writeStream
        .format("json")
        .option("path", "SaveDatalake")
        .option("checkpointLocation", "checkpoint")
        .outputMode("append")
        .trigger(Trigger.ProcessingTime("1 minute"))
        .start()

      println("Stream query started successfully.")
      
      // Await termination
      query.awaitTermination()
    } catch {
      case e: Exception => 
        println(s"An error occurred: ${e.getMessage}")
        e.printStackTrace()
    }

    println("Drone Data Processor job completed.")
  }
}
