import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object DataAnalysis {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("Drone Data Analysis")
      .master("local[*]")
      .config("spark.hadoop.fs.s3a.endpoint", "http://localhost:9000")
      .config("spark.hadoop.fs.s3a.access.key", "StrongPass!2024")
      .config("spark.hadoop.fs.s3a.secret.key", "hadoopUser123")
      .config("spark.hadoop.fs.s3a.path.style.access", "true")
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .getOrCreate()

    import spark.implicits._

    // Lire les fichiers JSON avec leurs chemins d'accès
    val droneDataDF = spark.read
      .format("json")
      .load("s3a://drone-data-lake/")
      .withColumn("input_file_name", input_file_name())

    // Extraire le nom du fichier à partir du chemin complet
    val droneDataWithFileName = droneDataDF.withColumn("file_name", regexp_extract($"input_file_name", "([^/]+$)", 1))

    // Créer une vue temporaire pour les requêtes SQL
    droneDataWithFileName.createOrReplaceTempView("drone_data")

    // Question 1: Nombre total d'alertes
    val totalAlerts = spark.sql("SELECT COUNT(*) AS total_alerts FROM drone_data WHERE dangerousity > 0.9")
    totalAlerts.show()

    // Question 2: Nombre d'alertes par jour de la semaine
    val alertsByDayOfWeek = spark.sql("""
      SELECT 
        CASE 
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 1 THEN 'Sunday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 2 THEN 'Monday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 3 THEN 'Tuesday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 4 THEN 'Wednesday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 5 THEN 'Thursday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 6 THEN 'Friday'
          WHEN dayofweek(from_unixtime(timestamp / 1000)) = 7 THEN 'Saturday'
        END AS day_of_week,
        COUNT(*) AS total_alerts
      FROM drone_data 
      WHERE dangerousity > 0.9
      GROUP BY dayofweek(from_unixtime(timestamp / 1000))
      ORDER BY total_alerts DESC
    """)
    alertsByDayOfWeek.show()

    // Question 3: Nombre d'alertes par heure de la journée
    val alertsByHourOfDay = spark.sql("""
      SELECT 
        hour(from_unixtime(timestamp / 1000)) AS hour_of_day,
        COUNT(*) AS total_alerts
      FROM drone_data 
      WHERE dangerousity > 0.9
      GROUP BY hour(from_unixtime(timestamp / 1000))
      ORDER BY total_alerts DESC
    """)
    alertsByHourOfDay.show()

    // Question 4: Nombre d'alertes par altitude et nom du fichier
    val alertsByAltitude = spark.sql("""
      SELECT 
        altitude,
        file_name,
        COUNT(*) AS total_alerts
      FROM drone_data 
      WHERE dangerousity > 0.9
      GROUP BY altitude, file_name
      ORDER BY total_alerts DESC
    """)
    alertsByAltitude.show()
  }
}