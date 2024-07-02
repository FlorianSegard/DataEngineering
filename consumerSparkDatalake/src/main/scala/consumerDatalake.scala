import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object ConsumerDatalake {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("Drone Data Processor")
      .master("local[*]")
      .config("spark.hadoop.fs.s3a.endpoint", "http://localhost:9000")
      .config("spark.hadoop.fs.s3a.access.key", "StrongPass2024")
      .config("spark.hadoop.fs.s3a.secret.key", "hadoopUser123")
      .config("spark.hadoop.fs.s3a.path.style.access", "true")
      .config("spark.executor.instances", "3")
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
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
      .option("subscribe", "drone-data")
      .option("group.id", "groupdatalake")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING) as message")

    // Désérialisation des messages JSON en DataFrame en utilisant le schéma défini
    val droneDataStream = rawStream
      .select(from_json($"message", droneDataSchema).as("data"))
      .select("data.*")
        
    // Écriture des données structurées dans MinIO
    val query = droneDataStream.writeStream
      .format("json")
      .option("path", "s3a://drone-data-lake/") // Écrire dans MinIO
      .option("checkpointLocation", "s3a://drone-data-lake/checkpoint/all-data")  // Checkpoint dans MinIO
      .outputMode("append")
      .trigger(Trigger.ProcessingTime("1 minute"))
      .start()
    
    query.awaitTermination()
  }
}
