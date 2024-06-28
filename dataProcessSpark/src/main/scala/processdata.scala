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

    // Définition du schéma JSON selon la structure de DroneData
    val droneDataSchema = new StructType()
      .add("id", IntegerType)
      .add("timestamp", LongType)
      .add("latitude", DoubleType)
      .add("longitude", DoubleType)
      .add("altitude", DoubleType)
      .add("dangerousity", DoubleType)

    // Lecture des données depuis Kafka
    val droneDataStream = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "drone-data")
      .load()
      .selectExpr("CAST(value AS STRING) as message")
      .select(from_json($"message", droneDataSchema).as("data"))
      .select("data.*")

    // Filtrage des données en fonction de la dangerousity
    val highRiskStream = droneDataStream
      .filter($"dangerousity" > 0.9)

    // Écriture des données filtrées dans un fichier (simulation d'un datalake)
    val query = highRiskStream
      .writeStream
      .outputMode("append")
      .format("csv")  // Vous pouvez choisir d'autres formats comme parquet ou json
      .option("path", "datalake")
      //.option("checkpointLocation", "path/to/your/checkpoint/directory")
      .trigger(Trigger.ProcessingTime("1 minute"))  // Déclenche l'écriture toutes les minutes
      .start()

    query.awaitTermination()
  }
}

